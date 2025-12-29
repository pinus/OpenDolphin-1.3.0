package open.dolphin.ui;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.lang.foreign.FunctionDescriptor.of;
import static java.lang.foreign.FunctionDescriptor.ofVoid;
import static java.lang.foreign.ValueLayout.*;

/// Input Source Selection via Foreign Function and Memory API
///
/// @author pns
public class IMEServer {
    static final Linker LINKER = Linker.nativeLinker();

    static final SymbolLookup LIB_APPKIT = SymbolLookup.libraryLookup("/System/Library/Frameworks/AppKit.framework/Versions/C/AppKit", Arena.global());
    static final SymbolLookup LIB_DISPATCH = SymbolLookup.libraryLookup("/usr/lib/system/libdispatch.dylib", Arena.global());
    static final SymbolLookup LIB_OBJC = SymbolLookup.libraryLookup("/usr/lib/libobjc.A.dylib", Arena.global());

    /// ----------- LIB_DISPATCH --------------
    // _dispatch_main_q はグローバル変数のアドレス - dispatch_get_main_queue はこれを返すマクロとして定義されている
    static final MemorySegment _dispatch_main_q = LIB_DISPATCH.find("_dispatch_main_q").orElseThrow();

    // void dispatch_sync_f(dispatch_queue_t queue, void * context, dispatch_function_t work);
    static final MethodHandle dispatch_sync_f = LINKER.downcallHandle(
        LIB_DISPATCH.findOrThrow("dispatch_sync_f"), ofVoid(ADDRESS, ADDRESS, ADDRESS)
    );

    /// ----------- LIB_OBJC --------------
    static class LibObjc {
        /// Structure to be passed as the context for dispatch_sync_f
        static StructLayout CONTEXT = MemoryLayout.structLayout(
            ADDRESS.withName("name"),     // cString
            ADDRESS.withName("classPtr"), // pointer
            ADDRESS.withName("selPtr"),   // pointer
            ADDRESS.withName("argPtr"),   // pointer
            JAVA_LONG.withName("argLong"),  // arg as an NSInteger
            ADDRESS.withName("resPtr"),   // response as a pointer
            JAVA_LONG.withName("resLong"), // response as an NSInteger
            JAVA_INT.withName("retain"), // whether to retain the response false = 0, true = 1
            JAVA_INT.withName("descCombination")
        );
        static VarHandle vhName = CONTEXT.varHandle(MemoryLayout.PathElement.groupElement("name"));
        static VarHandle vhClassPtr = CONTEXT.varHandle(MemoryLayout.PathElement.groupElement("classPtr"));
        static VarHandle vhSelPtr = CONTEXT.varHandle(MemoryLayout.PathElement.groupElement("selPtr"));
        static VarHandle vhArgPtr = CONTEXT.varHandle(MemoryLayout.PathElement.groupElement("argPtr"));
        static VarHandle vhArgLong = CONTEXT.varHandle(MemoryLayout.PathElement.groupElement("argLong"));
        static VarHandle vhResPtr = CONTEXT.varHandle(MemoryLayout.PathElement.groupElement("resPtr"));
        static VarHandle vhRetain = CONTEXT.varHandle(MemoryLayout.PathElement.groupElement("retain"));
        static VarHandle vhDescCombination = CONTEXT.varHandle(MemoryLayout.PathElement.groupElement("descCombination"));
        static VarHandle vhResLong = CONTEXT.varHandle(MemoryLayout.PathElement.groupElement("resLong"));

        /// Create a stub for the Java method handle to be passed as the work argument to dispatch_sync_f.
        static MemorySegment workFactory(String method) {
            try {
                MethodHandle handleJava = MethodHandles.lookup().
                    findStatic(LibObjc.class, method, MethodType.methodType(void.class, MemorySegment.class));
                return LINKER.upcallStub(handleJava, ofVoid(ADDRESS), Arena.ofAuto());

            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        /// id objc_getClass(const char * name);
        static MemorySegment objc_getClass(String name) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment context = arena.allocate(CONTEXT);
                vhName.set(context, 0, arena.allocateFrom(name));
                MemorySegment work = workFactory("objc_getclass_native");
                dispatch_sync_f.invokeExact(_dispatch_main_q, context, work);
                return (MemorySegment) vhClassPtr.get(context, 0);

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /// Called via dispatch_sync_f and executes on the main queue (_dispatch_main_q).
        static void objc_getclass_native(MemorySegment cContext) {
            // The context is returned as a C pointer, so it needs to be reinterpreted.
            MemorySegment context = cContext.reinterpret(CONTEXT.byteSize());
            MemorySegment classNamePtr = (MemorySegment) vhName.get(context, 0);

            try {
                MethodHandle mh = LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_getClass"), of(ADDRESS, ADDRESS));
                MemorySegment classPtr = (MemorySegment) mh.invokeExact(classNamePtr);
                vhClassPtr.set(context, 0, classPtr);

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /// id sel_registerName(const char *str)
        static MemorySegment sel_registerName(String name) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment context = arena.allocate(CONTEXT);
                vhName.set(context, 0, arena.allocateFrom(name));
                MemorySegment work = workFactory("sel_registerName_native");
                dispatch_sync_f.invokeExact(_dispatch_main_q, context, work);
                return (MemorySegment) vhSelPtr.get(context, 0);

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /// Called via dispatch_sync_f and executes on the main queue (_dispatch_main_q).
        static void sel_registerName_native(MemorySegment cContext) {
            // reinterpret context c pointer
            MemorySegment context = cContext.reinterpret(CONTEXT.byteSize());
            MemorySegment selNamePtr = (MemorySegment) vhName.get(context, 0);
            try {
                MethodHandle mh = LINKER.downcallHandle(LIB_OBJC.findOrThrow("sel_registerName"), of(ADDRESS, ADDRESS));
                MemorySegment selPtr = (MemorySegment) mh.invokeExact(selNamePtr);
                vhSelPtr.set(context, 0, selPtr);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /// id objc_msgSend(id self, SEL op, arg...)
        // Takes Class and Selector and returns a MemorySegment.
        static Object objc_msgSend(MemorySegment classPtr, MemorySegment selPtr, boolean shouldRetain) {
            return objc_msgSend(classPtr, selPtr, MemorySegment.NULL, 0, shouldRetain, 1); // of(ADDRESS, ADDRESS, ADDRESS)
        }

        // Takes Class, Selector and an Argument of MemorySegment and returns a MemorySegment.
        static Object objc_msgSend(MemorySegment classPtr, MemorySegment selPtr, MemorySegment argPtr, boolean shouldRetain) {
            return objc_msgSend(classPtr, selPtr, argPtr, 0, shouldRetain, 0); // of(ADDRESS, ADDRESS, ADDRESS, ADDRESS)
        }

        // Takes Class, Selector and an Argument of Long and returns a MemorySegment.
        static Object objc_msgSend(MemorySegment classPtr, MemorySegment selPtr, long argLong, boolean shouldRetain) {
            return objc_msgSend(classPtr, selPtr, MemorySegment.NULL, argLong, shouldRetain, 4); // of(ADDRESS, ADDRESS, ADDRESS, JAVA_LONG)
        }

        // Takes Class, Selector and an Argument of MemorySegment and returns nothing.
        static void objc_msgSend_void(MemorySegment classPtr, MemorySegment selPtr, MemorySegment argPtr) {
            objc_msgSend(classPtr, selPtr, argPtr, 0, false, 5);
        }

        // Takes Class, Selector and an Argument of MemorySegment or Long and returns a MemorySegment.
        // The combination of FunctionDescriptor is selectable via descCombination integer.
        static Object objc_msgSend(MemorySegment classPtr, MemorySegment selPtr, MemorySegment argPtr, long argLong, boolean shouldRetain, int descCombination) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment context = arena.allocate(CONTEXT);
                vhClassPtr.set(context, 0, classPtr);
                vhSelPtr.set(context, 0, selPtr);
                vhArgPtr.set(context, 0, argPtr);
                vhArgLong.set(context, 0, argLong);
                vhRetain.set(context, 0, shouldRetain ? 1 : 0); // false = 0
                vhDescCombination.set(context, 0, descCombination); // default = of(ADDRESS, ADDRESS, ADDRESS, ADDRESS)
                MemorySegment work = workFactory("objc_msgSend_native");
                dispatch_sync_f.invokeExact(_dispatch_main_q, context, work);

                return descCombination == 3 ? // of(JAVA_LONG, ADDRESS, ADDRESS)
                    (long) vhResLong.get(context, 0) : (MemorySegment) vhResPtr.get(context, 0);

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /// Called via dispatch_sync_f and executes on the main queue (_dispatch_main_q).
        static void objc_msgSend_native(MemorySegment cContext) {
            // reinterpret context c pointer
            MemorySegment context = cContext.reinterpret(CONTEXT.byteSize());
            MemorySegment classPtr = (MemorySegment) vhClassPtr.get(context, 0);
            MemorySegment selPtr = (MemorySegment) vhSelPtr.get(context, 0);
            MemorySegment argPtr = (MemorySegment) vhArgPtr.get(context, 0);
            long argLong = (long) vhArgLong.get(context, 0);
            int shouldRetain = (int) vhRetain.get(context, 0);
            int descCombination = (int) vhDescCombination.get(context, 0);

            try (Arena arena = Arena.ofConfined()) {
                MemorySegment resPtr = MemorySegment.NULL;
                long resLong = 0;
                switch (descCombination) {
                    case 1 -> {
                        MethodHandle mh = LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_msgSend"), of(ADDRESS, ADDRESS, ADDRESS));
                        resPtr = (MemorySegment) mh.invokeExact(classPtr, selPtr);
                    }
                    case 2 -> { // ex) release
                        MethodHandle mh = LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_msgSend"), ofVoid(ADDRESS, ADDRESS));
                        mh.invokeExact(classPtr, selPtr);
                    }
                    case 3 -> {
                        MethodHandle mh = LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_msgSend"), of(JAVA_LONG, ADDRESS, ADDRESS));
                        resLong = (long) mh.invokeExact(classPtr, selPtr);
                    }
                    case 4 -> {
                        MethodHandle mh = LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_msgSend"), of(ADDRESS, ADDRESS, ADDRESS, JAVA_LONG));
                        resPtr = (MemorySegment) mh.invokeExact(classPtr, selPtr, argLong);
                    }
                    case 5 -> {
                        MethodHandle mh = LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_msgSend"), ofVoid(ADDRESS, ADDRESS, ADDRESS));
                        mh.invokeExact(classPtr, selPtr, argPtr);
                    }
                    default -> {
                        MethodHandle mh = LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_msgSend"), of(ADDRESS, ADDRESS, ADDRESS, ADDRESS));
                        resPtr = (MemorySegment) mh.invokeExact(classPtr, selPtr, argPtr);
                    }
                }

                // Objective-C オブジェクトの場合 autorelease されるので retain が必須
                // 戻り値が C ポインタ(char* 等)やプリミティブの場合は retain してはダメ
                if (!resPtr.equals(MemorySegment.NULL) && shouldRetain != 0) { // 0 == false
                    resPtr = retain_native((MemorySegment) resPtr, arena);
                }
                vhResPtr.set(context, 0, resPtr);
                vhResLong.set(context, 0, resLong);

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /// Retains an Objective‑C object. Should be called on the _dispatch_main_q.
        static MemorySegment retain_native(MemorySegment obj, Arena arena) throws Throwable {
            MethodHandle retain = LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_msgSend"), of(ADDRESS, ADDRESS, ADDRESS));
            MemorySegment sel_retain = (MemorySegment) LINKER.downcallHandle(
                LIB_OBJC.findOrThrow("sel_registerName"), of(ADDRESS, ADDRESS)).invokeExact(arena.allocateFrom("retain"));
            return (MemorySegment) retain.invokeExact(obj, sel_retain);
        }

        /// Releases an Objective‑C object. Should be called on the java thread.
        static void release(MemorySegment objPtr) {
            if (objPtr.equals(MemorySegment.NULL)) return;

            try (Arena arena = Arena.ofConfined()) {
                MemorySegment context = arena.allocate(CONTEXT);
                vhClassPtr.set(context, 0, objPtr); // 第1引数: 対象オブジェクト
                vhSelPtr.set(context, 0, sel_registerName("release")); // 第2引数: releaseセレクタ
                vhArgPtr.set(context, 0, MemorySegment.NULL);
                vhRetain.set(context, 0, 0); // false = 0
                vhDescCombination.set(context, 0, 2); // ofVoid(ADDRESS, ADDRESS)
                MemorySegment work = workFactory("objc_msgSend_native");
                dispatch_sync_f.invokeExact(_dispatch_main_q, context, work);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /// ----------- NSTextInputContext Related --------------
    static class NSTextInputContext {
        static MemorySegment cls_NSTextInputContext;
        static MemorySegment sel_currentInputContext;
        static MemorySegment sel_selectedKeyboardInputSource;
        static MemorySegment sel_keyboardInputSources;
        static MemorySegment sel_setSelectedKeyboardInputSource;
        static MemorySegment currentInputContext;

        static void init() {
            if (cls_NSTextInputContext != null) { return; } // already initialized
            cls_NSTextInputContext = LibObjc.objc_getClass("NSTextInputContext");
            sel_currentInputContext = LibObjc.sel_registerName("currentInputContext");
            sel_selectedKeyboardInputSource = LibObjc.sel_registerName("selectedKeyboardInputSource");
            sel_keyboardInputSources = LibObjc.sel_registerName("keyboardInputSources");
            sel_setSelectedKeyboardInputSource = LibObjc.sel_registerName("setSelectedKeyboardInputSource:");
            currentInputContext = (MemorySegment) LibObjc.objc_msgSend(cls_NSTextInputContext, sel_currentInputContext, true);
        }

        static List<String> keyboardInputSources() {
            init();
            List<String> list = new ArrayList<>();
            // NSArray<NSString *> *
            MemorySegment nsArray = (MemorySegment) LibObjc.objc_msgSend(currentInputContext, sel_keyboardInputSources, true);
            long count = nsArrayCount(nsArray);
            for (long i = 0; i < count; i++) {
                // Creates new objects so that NSObjects can be safely released.
                list.add(nsStringToString(objectAtIndex(nsArray, i)));
            }
            LibObjc.release(nsArray);
            return list;
        }

        static String selectedInputSource() {
            init();
            MemorySegment nsSelectedInputSource = (MemorySegment) LibObjc.objc_msgSend(currentInputContext, sel_selectedKeyboardInputSource, true);
            // Creates a new object so that the NSObject can be safely released.
            String inputSourceId = nsStringToString(nsSelectedInputSource);
            LibObjc.release(nsSelectedInputSource);
            return inputSourceId;
        }

        static void setSelectedInputSource(String inputSourceId) {
            String selectedInputSourceId = selectedInputSource();
            if (!inputSourceId.equals(selectedInputSourceId)) {
                try (Arena arena = Arena.ofConfined()) {
                    MemorySegment cStr = arena.allocateFrom(inputSourceId);
                    MemorySegment nsInputSource = cStringToNSString(cStr);
                    LibObjc.objc_msgSend_void(currentInputContext, sel_setSelectedKeyboardInputSource, nsInputSource);
                    LibObjc.release(nsInputSource);
                }
            } // else { IO.println("already selected"); }
        }
    }

    /// Utilities
    static MemorySegment nsStringToCString(MemorySegment nsString) {
        MemorySegment sel_UTF8String = LibObjc.sel_registerName("UTF8String");
        // 戻り値は char * なので, retain してはいけない
        return (MemorySegment) LibObjc.objc_msgSend(nsString, sel_UTF8String, false);
    }

    static String cStringToString(MemorySegment cString) {
        // 最大 10MB までの範囲でヌル文字を探すよう指示
        return cString.reinterpret(10 * 1024 * 1024).getString(0, StandardCharsets.UTF_8);
    }

    static String nsStringToString(MemorySegment nsString) {
        // NSString の release は自己責任
        return cStringToString(nsStringToCString(nsString));
    }

    static MemorySegment cStringToNSString(MemorySegment cString) {
        MemorySegment cls_NSString = LibObjc.objc_getClass("NSString");
        MemorySegment sel_stringWithUTF8String = LibObjc.sel_registerName("stringWithUTF8String:");
        return (MemorySegment) LibObjc.objc_msgSend(cls_NSString, sel_stringWithUTF8String, cString, true);
    }

    static long nsArrayCount(MemorySegment nsArray) {
        MemorySegment sel_count = LibObjc.sel_registerName("count");
        return (long) LibObjc.objc_msgSend(nsArray, sel_count, MemorySegment.NULL, 0, false, 3); // of(JAVA_LONG, ADDRESS, ADDRESS)
    }

    static MemorySegment objectAtIndex(MemorySegment nsArray, long index) {
        return (MemorySegment) LibObjc.objc_msgSend(nsArray, LibObjc.sel_registerName("objectAtIndex:"), index, true); // of(ADDRESS, ADDRESS, ADDRESS, JAVA_LONG)
    }

    /// Main part
    static String abcId;
    static String usId;
    static String usExtendedId;
    static String japaneseId;
    static String katakanaId;
    static String romanId;

    static boolean inputSourcesInitialized() {
        if (abcId != null || usId != null || usExtendedId != null || japaneseId != null || katakanaId != null || romanId != null) {
            return true;
        }
        String atokId = null, kotoeriId = null;
        for (String sourceId : keyboardInputSources()) {
            if ("com.apple.keylayout.ABC".equals(sourceId)) {
                abcId = sourceId;
            } else if ("com.apple.keylayout.US".equals(sourceId)) {
                usId = sourceId;
            } else if ("com.apple.keylayout.USExtended".equals(sourceId)) {
                usExtendedId = sourceId;
            } else if (sourceId.contains("justsystem")) {
                if (sourceId.endsWith(".Japanese")) {
                    atokId = sourceId;
                } else if (sourceId.endsWith(".Roman")) {
                    romanId = sourceId;
                } else if (sourceId.endsWith(".Katakana")) {
                    katakanaId = sourceId;
                }
            } else if (sourceId.contains("Kotoeri")) {
                if (sourceId.contains(".Japanese")) {
                    kotoeriId = sourceId;
                }
            }
        }
        // atok を優先
        if (atokId != null) {
            japaneseId = atokId;
        } else if (kotoeriId != null) {
            japaneseId = kotoeriId;
        }
        return false;
    }

    static List<String> keyboardInputSources() {
        return NSTextInputContext.keyboardInputSources();
    }

    static void select(String inputSourcdId) {
        if (!inputSourcesInitialized()) { return; }
        NSTextInputContext.setSelectedInputSource(inputSourcdId);
    }

    static void selectABC() { select(abcId); }
    static void selectJapanese() { select(japaneseId); }
    static void selectKatakana() { select(katakanaId); }
    static void selectRoman() { select(romanId); }
    static void selectUS() { select(usId); }
    static void selectUSExtended() { select(usExtendedId); }
}

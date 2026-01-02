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
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

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

    /// Receiver as a work for dispatch_sync_f
    interface Receiver {
        void receive(MemorySegment context);
    }

    // Dispatched work calls Receiver.receive(context)
    static final MethodHandle mh_dispatchTask;
    static {
        try {
            mh_dispatchTask = MethodHandles.lookup().findVirtual(Receiver.class, "receive", MethodType.methodType(void.class, MemorySegment.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // Struct to be allocated as the context for dispatch_sync_f
    static final StructLayout STRUCT = MemoryLayout.structLayout(
        ADDRESS.withName("classPtr"),  // pointer
        ADDRESS.withName("selPtr"),    // pointer
        ADDRESS.withName("argPtr"),    // pointer
        ADDRESS.withName("resPtr")    // response as a pointer
    );
    static final VarHandle vhClassPtr = STRUCT.varHandle(MemoryLayout.PathElement.groupElement("classPtr"));
    static final VarHandle vhSelPtr = STRUCT.varHandle(MemoryLayout.PathElement.groupElement("selPtr"));
    static final VarHandle vhArgPtr = STRUCT.varHandle(MemoryLayout.PathElement.groupElement("argPtr"));
    static final VarHandle vhResPtr = STRUCT.varHandle(MemoryLayout.PathElement.groupElement("resPtr"));

    /// ----------- LIB_OBJC --------------
    static class LibObjc {

        /// id objc_getClass(const char * name);
        static final MethodHandle mh_objc_getClass = LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_getClass"), of(ADDRESS, ADDRESS));
        static MemorySegment objc_getClass(String name) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment cName = arena.allocateFrom(name);
                return (MemorySegment) mh_objc_getClass.invokeExact(cName);

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /// id sel_registerName(const char * str)
        static final MethodHandle mh_sel_registerName = LINKER.downcallHandle(LIB_OBJC.findOrThrow("sel_registerName"), of(ADDRESS, ADDRESS));
        static MemorySegment sel_registerName(String name) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment cName = arena.allocateFrom(name);
                return (MemorySegment) mh_sel_registerName.invokeExact(cName);

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /// - (void) retain
        static MemorySegment retain(MemorySegment objPtr) {
            try {
                return (MemorySegment) mh_objc_msgSend[AAA].invokeExact(objPtr, sel_registerName("retain"));

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /// - (void) release
        static void release(MemorySegment objPtr) {
            try {
                if (objPtr.equals(MemorySegment.NULL)) { return; }
                mh_objc_msgSend[VAA].invokeExact(objPtr, sel_registerName("release"));

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /// (void *) objc_msgSend(id self, SEL op, arg...)
        /// The combination of FunctionDescriptor is selectable via desc integer (AAAA, AAA, etc...)
        static final MethodHandle[] mh_objc_msgSend = {
            LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_msgSend"), of(ADDRESS, ADDRESS, ADDRESS, ADDRESS)),
            LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_msgSend"), of(ADDRESS, ADDRESS, ADDRESS)),
            LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_msgSend"), ofVoid(ADDRESS, ADDRESS)),
            LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_msgSend"), of(JAVA_LONG, ADDRESS, ADDRESS)),
            LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_msgSend"), of(ADDRESS, ADDRESS, ADDRESS, JAVA_LONG)),
            LINKER.downcallHandle(LIB_OBJC.findOrThrow("objc_msgSend"), ofVoid(ADDRESS, ADDRESS, ADDRESS)),
        };
        static final int AAAA = 0, AAA = 1, VAA = 2, LAA = 3, AAAL = 4, VAAA = 5;

        /// receiver から upcallStub を作って dispatch_sync_f にわたす
        static void dispatchSync(MemorySegment context, Receiver receiver, Arena arena) throws Throwable {
            var mh = mh_dispatchTask.bindTo(receiver);
            var work = LINKER.upcallStub(mh, ofVoid(ADDRESS), arena);
            dispatch_sync_f.invokeExact(_dispatch_main_q, context, work);
        }
    }

    /// ----------- NSTextInputContext Related --------------
    static class NSTextInputContext {
        static MemorySegment cls_NSTextInputContext;
        static MemorySegment sel_currentInputContext;
        static MemorySegment sel_selectedKeyboardInputSource;
        static MemorySegment sel_keyboardInputSources;
        static MemorySegment sel_setSelectedKeyboardInputSource;
        static MemorySegment current = MemorySegment.NULL;

        static void init() {
            if (!MemorySegment.NULL.equals(current)) { return; } // already initialized
            cls_NSTextInputContext = LibObjc.objc_getClass("NSTextInputContext");
            sel_currentInputContext = LibObjc.sel_registerName("currentInputContext");
            sel_selectedKeyboardInputSource = LibObjc.sel_registerName("selectedKeyboardInputSource");
            sel_keyboardInputSources = LibObjc.sel_registerName("keyboardInputSources");
            sel_setSelectedKeyboardInputSource = LibObjc.sel_registerName("setSelectedKeyboardInputSource:");
            current = msgSend_mainq(cls_NSTextInputContext, sel_currentInputContext);
        }

        ///  Helper method to call objc_msgSend with AAA descriptor
        static MemorySegment msgSend_mainq(MemorySegment classPtr, MemorySegment selPtr) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment context = arena.allocate(STRUCT);
                vhClassPtr.set(context, 0, classPtr);
                vhSelPtr.set(context, 0, selPtr);

                Receiver receiver = cContext -> {
                    try {
                        // The context is returned as a C pointer, so it needs to be reinterpreted.
                        MemorySegment ctx = cContext.reinterpret(STRUCT.byteSize());
                        MemorySegment cls = (MemorySegment) vhClassPtr.get(ctx, 0);
                        MemorySegment sel = (MemorySegment) vhSelPtr.get(ctx, 0);
                        MemorySegment res = (MemorySegment) LibObjc.mh_objc_msgSend[LibObjc.AAA].invokeExact(cls, sel);
                        vhResPtr.set(ctx, 0, res);

                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                };
                LibObjc.dispatchSync(context, receiver, arena);
                MemorySegment resPtr = (MemorySegment) vhResPtr.get(context, 0);
                // Objective-C オブジェクトの場合 autorelease されるので retain が必須
                return LibObjc.retain(resPtr);

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        ///  Helper method to call objc_msgSend with VAAA descriptor
        static void msgSend_mainq(MemorySegment classPtr, MemorySegment selPtr, MemorySegment argPtr) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment context = arena.allocate(STRUCT);
                vhClassPtr.set(context, 0, classPtr);
                vhSelPtr.set(context, 0, selPtr);
                vhArgPtr.set(context, 0, argPtr);

                Receiver receiver = cContext -> {
                    try {
                        MemorySegment ctx = cContext.reinterpret(STRUCT.byteSize());
                        MemorySegment cls = (MemorySegment) vhClassPtr.get(ctx, 0);
                        MemorySegment sel = (MemorySegment) vhSelPtr.get(ctx, 0);
                        MemorySegment arg = (MemorySegment) vhArgPtr.get(ctx, 0);
                        LibObjc.mh_objc_msgSend[LibObjc.VAAA].invokeExact(cls, sel, arg);

                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                };
                LibObjc.dispatchSync(context, receiver, arena);

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /// NSArray<NSString *> * keyboardInputSources;
        static List<String> keyboardInputSources() {
            init();
            List<String> list = new ArrayList<>();
            // NSArray<NSString *> *
            MemorySegment nsArray = msgSend_mainq(current, sel_keyboardInputSources);
            long count = nsArrayCount(nsArray);
            for (long i = 0; i < count; i++) {
                // Creates new objects so that NSObjects can be safely released.
                list.add(nsStringToString(objectAtIndex(nsArray, i)));
            }
            LibObjc.release(nsArray);
            return list;
        }

        /// NSString * selectedKeyboardInputSource;
        static String selectedInputSource() {
            init();
            MemorySegment nsSelectedInputSource = msgSend_mainq(current, sel_selectedKeyboardInputSource);
            // Creates a new object so that the NSObject can be safely released.
            String inputSourceId = nsStringToString(nsSelectedInputSource);
            LibObjc.release(nsSelectedInputSource);
            return inputSourceId;
        }

        /// void setSelectedKeyboardInputSource(NSString * inputSourceId);
        static void setSelectedInputSource(String inputSourceId) {
            String selectedInputSourceId = selectedInputSource();
            if (!inputSourceId.equals(selectedInputSourceId)) {
                try (Arena arena = Arena.ofConfined()) {
                    MemorySegment cStr = arena.allocateFrom(inputSourceId);
                    MemorySegment nsInputSource = cStringToNSString(cStr);
                    msgSend_mainq(current, sel_setSelectedKeyboardInputSource, nsInputSource);
                    LibObjc.release(nsInputSource);
                }
            }  //else { IO.println("already selected"); }
        }
    }

    /// Utilities
    static MemorySegment nsStringToCString(MemorySegment nsString) {
        try {
            MemorySegment sel_UTF8String = LibObjc.sel_registerName("UTF8String");
            return (MemorySegment) LibObjc.mh_objc_msgSend[LibObjc.AAA].invokeExact(nsString, sel_UTF8String);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static String cStringToString(MemorySegment cString) {
        return cString.reinterpret(Integer.MAX_VALUE).getString(0, StandardCharsets.UTF_8);
    }

    static String nsStringToString(MemorySegment nsString) {
        // NSString の release は自己責任
        return cStringToString(nsStringToCString(nsString));
    }

    static MemorySegment cStringToNSString(MemorySegment cString) {
        try {
            MemorySegment cls_NSString = LibObjc.objc_getClass("NSString");
            MemorySegment sel_stringWithUTF8String = LibObjc.sel_registerName("stringWithUTF8String:");
            MemorySegment nsString = (MemorySegment) LibObjc.mh_objc_msgSend[LibObjc.AAAA].invokeExact(cls_NSString, sel_stringWithUTF8String, cString);
            return LibObjc.retain(nsString);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static long nsArrayCount(MemorySegment nsArray) {
        try {
            MemorySegment sel_count = LibObjc.sel_registerName("count");
            return (long) LibObjc.mh_objc_msgSend[LibObjc.LAA].invokeExact(nsArray, sel_count);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static MemorySegment objectAtIndex(MemorySegment nsArray, long index) {
        try {
            MemorySegment sel_objectAtIndex = LibObjc.sel_registerName("objectAtIndex:");
            return (MemorySegment) LibObjc.mh_objc_msgSend[LibObjc.AAAL].invokeExact(nsArray, sel_objectAtIndex, index);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /// Main part
    static String abcId;
    static String usId;
    static String usExtendedId;
    static String japaneseId;
    static String katakanaId;
    static String romanId;

    static boolean initialized() {
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

    static void select(String inputSourceId) {
        Thread.ofPlatform().start(() ->{
            // Invoking on the AWT-EventQueue results in a deadlock.
            if (!initialized()) { return; }
            NSTextInputContext.setSelectedInputSource(inputSourceId);
        });
    }

    static void selectABC() { select(abcId); }
    static void selectJapanese() { select(japaneseId); }
    static void selectKatakana() { select(katakanaId); }
    static void selectRoman() { select(romanId); }
    static void selectUS() { select(usId); }
    static void selectUSExtended() { select(usExtendedId); }
}

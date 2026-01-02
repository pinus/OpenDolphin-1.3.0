package open.dolphin.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.stream.Stream;

import static java.lang.foreign.FunctionDescriptor.of;
import static java.lang.foreign.FunctionDescriptor.ofVoid;
import static java.lang.foreign.ValueLayout.*;

/// Text Input Source Selection via Foreign Function and Memory API
///
/// @author pns
public class IMEServerTIS {
    static final Logger logger = LoggerFactory.getLogger(IMEServerTIS.class);
    static final Linker LINKER = Linker.nativeLinker();
    static final SymbolLookup HITOOLBOX = SymbolLookup.libraryLookup("/System/Library/Frameworks/Carbon.framework/Carbon", Arena.global());
    static final MemorySegment _dispatch_main_q = HITOOLBOX.find("_dispatch_main_q").orElseThrow();
    static final MethodHandle dispatch_sync_f = LINKER.downcallHandle(HITOOLBOX.findOrThrow("dispatch_sync_f"), ofVoid(ADDRESS, ADDRESS, ADDRESS));

    // Dispatched work calls Receiver.receive(context)
    static final MethodHandle mh_dispatchTask;
    static {
        try {
            mh_dispatchTask = MethodHandles.lookup().findVirtual(Receiver.class, "receive", MethodType.methodType(void.class, MemorySegment.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // method handles
    static final MethodHandle mh_CFArrayGetCount = LINKER.downcallHandle(HITOOLBOX.findOrThrow("CFArrayGetCount"), of(JAVA_LONG, ADDRESS));
    static final MethodHandle mh_CFArrayGetValueAtIndex = LINKER.downcallHandle(HITOOLBOX.findOrThrow("CFArrayGetValueAtIndex"), of(ADDRESS, ADDRESS, JAVA_LONG));
    static final MethodHandle mh_GetProperty = LINKER.downcallHandle(HITOOLBOX.find("TISGetInputSourceProperty").orElseThrow(), of(ADDRESS, ADDRESS, ADDRESS));
    static final MethodHandle mh_TISCreateinputSourceList = LINKER.downcallHandle(HITOOLBOX.find("TISCreateInputSourceList").orElseThrow(), of(ADDRESS, ADDRESS, JAVA_BOOLEAN));
    static final MethodHandle mh_TISSelectInputSource = LINKER.downcallHandle(HITOOLBOX.find("TISSelectInputSource").orElseThrow(), of(JAVA_INT, ADDRESS));

    // struct to be allocated on context
    static final StructLayout STRUCT = MemoryLayout.structLayout(ADDRESS.withName("resPtr"), ADDRESS.withName("arg1"), ADDRESS.withName("arg2"));
    static final VarHandle vhArg1 = STRUCT.varHandle(PathElement.groupElement("arg1"));
    static final VarHandle vhArg2 = STRUCT.varHandle(PathElement.groupElement("arg2"));
    static final VarHandle vhResPtr = STRUCT.varHandle(PathElement.groupElement("resPtr"));

    // properties are kept on memory
    static final MemorySegment kTISPropertyInputSourceID = propertyFor("kTISPropertyInputSourceID");
    static final MemorySegment kTISPropertyInputSourceIsSelected = propertyFor("kTISPropertyInputSourceIsSelected");
    static final MemorySegment kCFBooleanTrue = propertyFor("kCFBooleanTrue");

    // input sources
    enum INPUT_SOURCE {
        ABC("com.apple.keylayout.ABC", MemorySegment.NULL),
        US("com.apple.keylayout.US", MemorySegment.NULL),
        US_EXTENDED("com.apple.keylayout.US-Extended", MemorySegment.NULL),
        ROMAN("", MemorySegment.NULL),
        JAPANESE("", MemorySegment.NULL),
        KATAKANA("", MemorySegment.NULL)
        ;
        String id;
        MemorySegment ref;

        INPUT_SOURCE(String id, MemorySegment ref) {
            this.id = id;
            this.ref = ref;
        }
    }

    public IMEServerTIS() {}

    static void selectJapanese() { select(INPUT_SOURCE.JAPANESE.ref); }
    static void selectRoman() { select(INPUT_SOURCE.ROMAN.ref); }
    static void selectABC() { select(INPUT_SOURCE.ABC.ref); }
    static void selectUS() { select(INPUT_SOURCE.US.ref); }
    static void selectUSExt() { select(INPUT_SOURCE.US_EXTENDED.ref); }
    static void selectKatakana() { select(INPUT_SOURCE.KATAKANA.ref); }

    /// initialize INPUT_SOURCE
    static boolean initialized() {
        if (!Stream.of(INPUT_SOURCE.values()).allMatch(source -> source.ref.equals(MemorySegment.NULL))) {
            return true; // already initialized
        }
        // initialize INPUT_SOURCE
        try (var arena = Arena.ofConfined()) {
            // allocate context
            var context = arena.allocate(STRUCT);
            // define receiver
            Receiver receiver = cContext -> {
                try {
                    // context should be reinterpreted
                    var ctx = cContext.reinterpret(STRUCT.byteSize());
                    // CFArrayRef TISCreateInputSourceList(CFDictionaryRef properties, Boolean includeAllInstalled)
                    var arrayRef = (MemorySegment) mh_TISCreateinputSourceList.invoke(MemorySegment.NULL, false);
                    vhResPtr.set(ctx, 0, arrayRef); // write response value on context
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            };
            dispatchSync(context, receiver, arena);

            var arrayRef = (MemorySegment) vhResPtr.get(context, 0); // read response value from context
            long count = (long) mh_CFArrayGetCount.invoke(arrayRef);

            for (long i = 0; i < count; i++) {
                // i 番目の inputSource を取り出す
                // (void *) CFArrayGetValueAtIndex(CFArrayRef theArray, CFIndex idx)
                var inputSourceRef = (MemorySegment) mh_CFArrayGetValueAtIndex.invoke(arrayRef, i);

                // Lookup by sourceId: com.apple.keylayout {.US, .USExtended, .ABC, ... }
                var sourceIdPtr = tisGetInputSourceProperty(inputSourceRef, kTISPropertyInputSourceID);
                var sourceID = cfStringToJavaString(sourceIdPtr);
                if (sourceID == null) { continue; }
                // inputSource を設定
                if (sourceID.equals(INPUT_SOURCE.ABC.id)) { INPUT_SOURCE.ABC.ref = inputSourceRef; }
                else if (sourceID.equals(INPUT_SOURCE.US.id)) { INPUT_SOURCE.US.ref = inputSourceRef; }
                else if (sourceID.equals(INPUT_SOURCE.US_EXTENDED.id)) { INPUT_SOURCE.US_EXTENDED.ref = inputSourceRef; }
                else if (sourceID.contains("justsystems")) {
                    if (sourceID.endsWith(".Japanese")) {
                        INPUT_SOURCE.JAPANESE.id = sourceID;
                        INPUT_SOURCE.JAPANESE.ref = inputSourceRef;
                    } else if (sourceID.endsWith(".Katakana")) {
                        INPUT_SOURCE.KATAKANA.id = sourceID;
                        INPUT_SOURCE.KATAKANA.ref = inputSourceRef;
                    } else if (sourceID.endsWith(".Roman")) {
                        INPUT_SOURCE.ROMAN.id = sourceID;
                        INPUT_SOURCE.ROMAN.ref = inputSourceRef;
                    }
                } else if (sourceID.endsWith(".Japanese") && INPUT_SOURCE.JAPANESE.ref.equals(MemorySegment.NULL)) {
                    INPUT_SOURCE.JAPANESE.id = sourceID;
                    INPUT_SOURCE.JAPANESE.ref = inputSourceRef;
                }
            }

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    ///  change input source according to inputSourceRef
    static void select(MemorySegment inputSourceRef) {
        // EDT で呼ぶと deadlock する
        Thread.ofPlatform().start(() -> {
            if (!initialized()) { return; }

            try (var arena = Arena.ofConfined()) {
                var isSelectedPtr = tisGetInputSourceProperty(inputSourceRef, kTISPropertyInputSourceIsSelected);
                if (!isSelectedPtr.equals(kCFBooleanTrue)) {
                    MemorySegment context = arena.allocate(STRUCT);
                    vhArg1.set(context, 0, inputSourceRef);

                    Receiver receiver = cContext -> {
                        try {
                            var ctx = cContext.reinterpret(STRUCT.byteSize());
                            var source = (MemorySegment) vhArg1.get(ctx, 0);
                            int status = (int) mh_TISSelectInputSource.invoke(source);
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    };
                    dispatchSync(context, receiver, arena);

                } else { logger.info("already selected"); }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    /// dispatch_sync_f にわたす work
    interface Receiver {
        void receive(MemorySegment context);
    }

    /// receiver から upcallStub を作って dispatch_sync_f にわたす
    static void dispatchSync(MemorySegment context, Receiver receiver, Arena arena) throws Throwable {
        var mh = mh_dispatchTask.bindTo(receiver);
        var work = LINKER.upcallStub(mh, ofVoid(ADDRESS), arena);
        dispatch_sync_f.invokeExact(_dispatch_main_q, context, work);
    }

    /// TISGetInputSourceProperty を呼んで, propertyKey に対応するプロパティーを取得する
    static MemorySegment tisGetInputSourceProperty(MemorySegment inputSourceRef, MemorySegment propertyKey) throws Throwable {
        try (var arena = Arena.ofConfined()) {
            var context = arena.allocate(STRUCT);
            vhArg1.set(context, 0, inputSourceRef);
            vhArg2.set(context, 0, propertyKey);

            Receiver receiver = cContext -> {
                try {
                    var ctx = cContext.reinterpret(STRUCT.byteSize());
                    var source = (MemorySegment) vhArg1.get(ctx, 0);
                    var property = (MemorySegment) vhArg2.get(ctx, 0);
                    // (void *) TISGetInputSourceProperty (TISInputSourceRef, CFStringRef propertyKey)
                    var prop = mh_GetProperty.invoke(source, property);
                    vhResPtr.set(ctx, 0, prop);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            };
            dispatchSync(context, receiver, arena);
            return (MemorySegment) vhResPtr.get(context, 0);
        }
    }

    /// プロパティー名を取得
    static MemorySegment propertyFor(String name) {
        // サイズ情報を持たないポインタ (サイズ 0)
        var keyRef = HITOOLBOX.find(name).orElseThrow();
        // 変数を try の外で宣言し、reinterpret を使ってサイズ情報を付与してから読み取ります
        var propertyRef = MemorySegment.NULL;
        try {
            // SymbolLookupが返すセグメントはサイズ0なので、そのままgetするとIndexOutOfBoundsExceptionになります.
            // reinterpretを使って、このポインタが指す先のサイズ（ここではアドレス1個分）を指定します.
            keyRef = keyRef.reinterpret(ADDRESS.byteSize());
            propertyRef = keyRef.get(ADDRESS, 0);
        } catch (Throwable e) {
            logger.error("Failed to retrieve property for {}", name);
        }
        return propertyRef;
    }

    /// CFStringRef -> Java String
    static String cfStringToJavaString(MemorySegment cfStringRef) throws Throwable {
        if (cfStringRef.equals(MemorySegment.NULL)) return null;

        // 1. 文字列の長さを取得 (CFStringGetLength)
        var mhGetLength = LINKER.downcallHandle(
            HITOOLBOX.find("CFStringGetLength").orElseThrow(),
            of(JAVA_LONG, ADDRESS)
        );
        long length = (long) mhGetLength.invoke(cfStringRef);
        // 2. バッファを確保して C文字列としてコピー (CFStringGetCString)
        // UTF-8 エンコーディングの ID: 0x08000100
        final int kCFStringEncodingUTF8 = 0x08000100;

        // 最大バイトサイズを計算する関数があればベストですが、今回は安全マージンを取って (length * 3 + 1) 確保します
        long bufferSize = length * 3 + 1;

        try (var localArena = Arena.ofShared()) {
            var mhGetCString = LINKER.downcallHandle(
                HITOOLBOX.find("CFStringGetCString").orElseThrow(),
                of(
                    JAVA_BOOLEAN, // 成功したか
                    ADDRESS,      // CFStringRef
                    ADDRESS,      // buffer
                    JAVA_LONG,    // bufferSize
                    JAVA_INT      // encoding
                )
            );
            var buffer = localArena.allocate(bufferSize);
            var success = (boolean) mhGetCString.invoke(cfStringRef, buffer, bufferSize, kCFStringEncodingUTF8);

            if (success) {
                return buffer.getString(0); // UTF-8として読み取る
            }
        }
        return null;
    }
}

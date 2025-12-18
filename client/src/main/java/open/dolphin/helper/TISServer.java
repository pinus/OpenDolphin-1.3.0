package open.dolphin.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

/// Experimental TISServer built with Foreign Function and Memory API
public class TISServer {
    static final String HITOOLBOX_PATH = "/System/Library/Frameworks/Carbon.framework/Carbon";
    static Logger logger = LoggerFactory.getLogger(TISServer.class);
    static Linker LINKER = Linker.nativeLinker();
    static SymbolLookup HITOOLBOX = SymbolLookup.libraryLookup(HITOOLBOX_PATH, Arena.global());

    public TISServer() {
    }

    public static void main(String[] args) throws Throwable {
        Thread.ofPlatform().start(TISServer::start);

        // to prevent the new icon from bouncing eternally
        var mh = LINKER.downcallHandle(HITOOLBOX.find("RunApplicationEventLoop").orElseThrow(),
            FunctionDescriptor.ofVoid()
        );
        mh.invoke();
    }

    static boolean start() {
        try (var br = new BufferedReader(new InputStreamReader(System.in))) {
            // 初期化
            tisCreateInputSourceList();

            String line;
            while ((line = br.readLine()) != null) {
                //IO.println("Input: " + line);
                switch (line.trim()) {
                    case "A" -> select(INPUT_SOURCE.ABC.ref);
                    case "U" -> select(INPUT_SOURCE.US.ref);
                    case "X" -> select(INPUT_SOURCE.US_EXTENDED.ref);
                    case "R" -> select(INPUT_SOURCE.ROMAN.ref);
                    case "J" -> select(INPUT_SOURCE.JAPANESE.ref);
                    case "K" -> select(INPUT_SOURCE.KATAKANA.ref);
                }
            }

        } catch (IOException e) {
            IO.println("Failed to read stdin: " + e.getMessage());
            return false;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /// Get the available input source list and set to INPUT_SOURCE
    static void tisCreateInputSourceList() throws Throwable {
        var mhTISCreateinputSourceList =
            LINKER.downcallHandle(HITOOLBOX.find("TISCreateInputSourceList").orElseThrow(),
                FunctionDescriptor.of(
                    ValueLayout.ADDRESS,     // CFArrayRef 戻り値
                    ValueLayout.ADDRESS,     // CFDictionaryRef (nullable) 第１引数
                    ValueLayout.JAVA_BOOLEAN // includeAllInstalled 第２引数
                )
            );

        var arrayRef = (MemorySegment) mhTISCreateinputSourceList.invoke(MemorySegment.NULL, false);

        var mhCFArrayGetCount = LINKER.downcallHandle(
            HITOOLBOX.findOrThrow("CFArrayGetCount"),
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );
        long count = (long) mhCFArrayGetCount.invoke(arrayRef);

        var kTISPropertyInputSourceID = propertyFor("kTISPropertyInputSourceID");
        var kTISPropertyInputModeID = propertyFor("kTISPropertyInputModeID");

        for (long i = 0; i < count; i++) {
            // i 番目の inputSource を取り出す
            var mh = LINKER.downcallHandle(
                HITOOLBOX.findOrThrow("CFArrayGetValueAtIndex"),
                FunctionDescriptor.of(
                    ValueLayout.ADDRESS,   // 戻り値: 要素のポインタ (void *)
                    ValueLayout.ADDRESS,   // 引数1: CFArrayRef
                    ValueLayout.JAVA_LONG  // 引数2: インデックス (CFIndex)
                )
            );
            var inputSourceRef = (MemorySegment) mh.invoke(arrayRef, i);

            // Lookup by sourceId: com.apple.keylayout {.US, .USExtended, .ABC, ... }
            var sourceIdPtr = tisGetInputSourceProperty(inputSourceRef, kTISPropertyInputSourceID);
            // CFStringRef を Java String に変換
            String sourceID = cfStringToJavaString(sourceIdPtr);
            // inputSource を設定
            if (INPUT_SOURCE.ABC.id.equals(sourceID)) {
                INPUT_SOURCE.ABC.ref = inputSourceRef;

            } else if (INPUT_SOURCE.US.id.equals(sourceID)) {
                INPUT_SOURCE.US.ref = inputSourceRef;

            } else if (INPUT_SOURCE.US_EXTENDED.id.equals(sourceID)) {
                INPUT_SOURCE.US_EXTENDED.ref = inputSourceRef;
            }

            // Lookup by mode: com.apple.inputmethod { .Roman, .Japanese, .Japanese.Katakana }
            // corresponding id = com.justsystems.inputmethod.atok35 {.Roman, .Japanese, .Japanese.Katakana }
            var modeIdPtr = tisGetInputSourceProperty(inputSourceRef, kTISPropertyInputModeID);
            var modeID = cfStringToJavaString(modeIdPtr);
            if (INPUT_SOURCE.JAPANESE.id.equals(modeID)) {
                INPUT_SOURCE.JAPANESE.ref = inputSourceRef;

            } else if (INPUT_SOURCE.ROMAN.id.equals(modeID)) {
                INPUT_SOURCE.ROMAN.ref = inputSourceRef;

            } else if (INPUT_SOURCE.KATAKANA.id.equals(modeID)) {
                INPUT_SOURCE.KATAKANA.ref = inputSourceRef;
            }
        }
    }

    static void select(MemorySegment inputSource) {
        try {
            var kTISPropertyInputSourceIsSelected = propertyFor("kTISPropertyInputSourceIsSelected");
            var kCFBooleanTrue = propertyFor("kCFBooleanTrue");
            var isSelectedPtr = tisGetInputSourceProperty(inputSource, kTISPropertyInputSourceIsSelected);
            if (isSelectedPtr.equals(kCFBooleanTrue)) {
                //logger.info("already selected");
                IO.println("OK");
                return;
            }
            var mh = LINKER.downcallHandle(
                HITOOLBOX.find("TISSelectInputSource").orElseThrow(),
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT,   // 戻り値: OSStatus (int)
                    ValueLayout.ADDRESS     // 引数: TISInputSourceRef
                )
            );
            int status = (int) mh.invoke(inputSource);
            if (status != 0) {
                logger.error("TISSelectInputSource failed with status: {}", status);
            } else {
                IO.println("OK");
            }
        } catch (Throwable e) {
            logger.error("Failed to select {}", e.getMessage());
        }
    }

    /// TISGetInputSourceProperty を呼んで, propertyKey に対応するプロパティーを取得する
    static MemorySegment tisGetInputSourceProperty(MemorySegment inputSourceRef, MemorySegment propertyKey) throws Throwable {
        MethodHandle mhGetProperty = LINKER.downcallHandle(
            HITOOLBOX.find("TISGetInputSourceProperty").orElseThrow(),
            FunctionDescriptor.of(
                ValueLayout.ADDRESS,     // 戻り値: プロパティの値 (CFTypeRef / void*)
                ValueLayout.ADDRESS,     // 引数1: TISInputSourceRef
                ValueLayout.ADDRESS      // 引数2: propertyKey (CFStringRef)
            )
        );
        // プロパティ取得の実行
        var prop = mhGetProperty.invoke(inputSourceRef, propertyKey);
        return (MemorySegment) prop;
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
            keyRef = keyRef.reinterpret(ValueLayout.ADDRESS.byteSize());
            propertyRef = keyRef.get(ValueLayout.ADDRESS, 0);
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
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );
        long length = (long) mhGetLength.invoke(cfStringRef);
        // 2. バッファを確保して C文字列としてコピー (CFStringGetCString)
        // UTF-8 エンコーディングの ID: 0x08000100
        final int kCFStringEncodingUTF8 = 0x08000100;

        // 最大バイトサイズを計算する関数があればベストですが、今回は安全マージンを取って (length * 3 + 1) 確保します
        long bufferSize = length * 3 + 1;

        try (Arena localArena = Arena.ofShared()) {
            var mhGetCString = LINKER.downcallHandle(
                HITOOLBOX.find("CFStringGetCString").orElseThrow(),
                FunctionDescriptor.of(
                    ValueLayout.JAVA_BOOLEAN, // 成功したか
                    ValueLayout.ADDRESS,      // CFStringRef
                    ValueLayout.ADDRESS,      // buffer
                    ValueLayout.JAVA_LONG,    // bufferSize
                    ValueLayout.JAVA_INT      // encoding
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

    /// Java String -> CFStringRef
    // Create や Copy という名前が含まれる Core Foundation の関数で作成したオブジェクトは、
    // 参照カウントが +1 された状態で返されます。
    // Java 側で使い終わったら CFRelease を呼ばないとメモリリークの原因になります。
    static MemorySegment javaStringToCFString(String string) throws Throwable {
        if (string == null) return MemorySegment.NULL;

        try (Arena localArena = Arena.ofConfined()) {
            // kCFStringEncodingUTF8 = 0x08000100
            final int kCFStringEncodingUTF8 = 0x08000100;

            var mhCreate = LINKER.downcallHandle(
                HITOOLBOX.find("CFStringCreateWithCString").orElseThrow(),
                FunctionDescriptor.of(
                    ValueLayout.ADDRESS,     // 戻り値: CFStringRef
                    ValueLayout.ADDRESS,     // allocator (NULLを指定)
                    ValueLayout.ADDRESS,     // cStr
                    ValueLayout.JAVA_INT     // encoding
                )
            );

            // Java String を一時的な C String に変換して渡す
            var cStr = localArena.allocateFrom(string);
            // allocator = NULL (Default)
            return (MemorySegment) mhCreate.invoke(MemorySegment.NULL, cStr, kCFStringEncodingUTF8);
        }
    }

    /// CFTypeRef (CFStringRef 等) を解放する
    static void cfRelease(MemorySegment cfTypeRef) throws Throwable {
        if (cfTypeRef.equals(MemorySegment.NULL)) return;
        var mhRelease = LINKER.downcallHandle(
            HITOOLBOX.find("CFRelease").orElseThrow(),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
        mhRelease.invoke(cfTypeRef);
    }

    enum INPUT_SOURCE {
        ABC("A", "com.apple.keylayout.ABC", MemorySegment.NULL),
        US("U", "com.apple.keylayout.US", MemorySegment.NULL),
        US_EXTENDED("X", "com.apple.keylayout.US-Extended", MemorySegment.NULL),
        ROMAN("R", "com.apple.inputmethod.Roman", MemorySegment.NULL),
        JAPANESE("J", "com.apple.inputmethod.Japanese", MemorySegment.NULL),
        KATAKANA("K", "com.apple.inputmethod.Japanese.Katakana", MemorySegment.NULL);
        final String trigger;
        final String id;
        MemorySegment ref;

        INPUT_SOURCE(String trigger, String id, MemorySegment ref) {
            this.trigger = trigger;
            this.id = id;
            this.ref = ref;
        }
    }
}

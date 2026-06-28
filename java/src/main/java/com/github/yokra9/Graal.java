package com.github.yokra9;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;
import org.graalvm.polyglot.*;

public class Graal {
    public static void main(String[] args) {
        System.out.println("os.name\t" + System.getProperty("os.name"));
        System.out.println("os.arch\t" + System.getProperty("os.arch"));
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                executeWasm();
                System.out.println("\nPress Enter to continue (Ctrl-C to exit)...");
                scanner.nextLine();
            }
        }
    }

    private static void executeWasm() {
        try (Context context = Context.create()) {
            File wasmFile = new File("count_kana.wasm");
            Value module = context.eval(Source.newBuilder("wasm", wasmFile).build());
            Value instance = module.newInstance();
            Value exports = instance.getMember("exports");

            Value alloc = exports.getMember("alloc");
            Value dealloc = exports.getMember("dealloc");
            Value countKana = exports.getMember("count_kana");
            Value freeResult = exports.getMember("free_result");
            Value memory = exports.getMember("memory");

            String message = "こんにちは、セカイ！";
            byte[] messageBytes = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            int len = messageBytes.length;
            int ptr = alloc.execute(len).asInt();

            // メモリに文字列データを書き込む
            for (int i = 0; i < len; i++) {
                memory.setArrayElement((long) ptr + i, (int) messageBytes[i]);
            }

            int resultPtr = countKana.execute(ptr, len).asInt();

            // メモリから CharTypeCount 構造体のデータを読み取る
            // 構造体レイアウト：i32 x 2 (各フィールド 4 バイト)
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < 8; i++) {
                Value byteVal = memory.getArrayElement((long) resultPtr + i);
                buffer.put(i, (byte) byteVal.asInt());
            }

            int hiragana = buffer.getInt(0);
            int katakana = buffer.getInt(4);

            // 結果を表示
            System.out.println("入力文字列:" + message);
            System.out.println("  ひらがな:" + hiragana);
            System.out.println("  全角カナ:" + katakana);

            // メモリを解放
            dealloc.execute(ptr, len);
            freeResult.execute(resultPtr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

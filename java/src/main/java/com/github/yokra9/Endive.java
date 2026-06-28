package com.github.yokra9;

import java.io.File;
import java.util.Scanner;
import run.endive.wasm.Parser;
import run.endive.runtime.*;

public class Endive {
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
        Instance instance = Instance.builder(Parser.parse(new File("count_kana.wasm"))).build();

        ExportFunction alloc = instance.export("alloc");
        ExportFunction dealloc = instance.export("dealloc");
        ExportFunction freeResult = instance.export("free_result");
        ExportFunction countCharTypes = instance.export("count_kana");

        Memory memory = instance.memory();

        String message = "こんにちは、セカイ！";
        byte[] messageBytes = message.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int len = messageBytes.length;

        // 文字列データをメモリに書き込む
        int ptr = (int) alloc.apply(len)[0];
        memory.write(ptr, messageBytes);

        // count_kana を呼び出し、結果の構造体ポインタを取得
        int resultPtr = (int) countCharTypes.apply(ptr, len)[0];

        // メモリから CharTypeCount 構造体のデータを読み取る
        // 構造体レイアウト：i32 x 2 (各フィールド 4 バイト)
        int hiragana = memory.readInt(resultPtr);
        int katakana = memory.readInt(resultPtr + 4);

        // 結果を表示
        System.out.println("入力文字列:" + message);
        System.out.println("  ひらがな:" + hiragana);
        System.out.println("  全角カナ:" + katakana);

        // メモリを解放
        dealloc.apply(ptr, len);
        freeResult.apply(resultPtr);
    }
}

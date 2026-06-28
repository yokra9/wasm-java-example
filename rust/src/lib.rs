use std::mem;
use std::slice;
use std::str;

/// 文字種別のカウント結果を格納する構造体
/// # Fields
/// * `hiragana` - ひらがなの数
/// * `katakana` - 全角カナの数
#[repr(C)]
pub struct CharTypeCount {
    pub hiragana: i32,
    pub katakana: i32,
}

/// メモリを割り当てる関数
/// # Arguments
/// * `len` - 割り当てるメモリの長さ
/// # Returns
/// 割り当てたメモリのポインタ
#[unsafe(no_mangle)]
pub extern "C" fn alloc(len: i32) -> *mut u8 {
    let mut buf = Vec::with_capacity(len as usize);
    let ptr = buf.as_mut_ptr();
    // Rust にクリーンアップしないように指示する
    mem::forget(buf);
    ptr
}

/// メモリを解放する関数
/// # Arguments
/// * `ptr` - 解放するメモリのポインタ
/// * `len` - 解放するメモリの長さ
#[unsafe(no_mangle)]
pub extern "C" fn dealloc(ptr: *mut u8, len: i32) {
    // メモリを解放する
    let _ = unsafe { Vec::from_raw_parts(ptr, 0, len as usize) };
}

/// count_kana が返した CharTypeCount を解放する関数
/// # Arguments
/// * `ptr` - count_kana が返した CharTypeCount ポインタ
#[unsafe(no_mangle)]
pub extern "C" fn free_result(ptr: i32) {
    if ptr == 0 {
        return;
    }
    let _ = unsafe { Box::from_raw(ptr as *mut CharTypeCount) };
}

/// 文字列の文字種別をカウント
/// # Arguments
/// * `ptr` - 文字列データのメモリアドレス
/// * `len` - 文字列データのバイト数
/// # Returns
/// CharTypeCount 構造体へのポインタ（メモリ割り当て済み）
#[unsafe(no_mangle)]
pub extern "C" fn count_kana(ptr: i32, len: i32) -> i32 {
    let bytes = unsafe { slice::from_raw_parts(ptr as *const u8, len as usize) };
    let s = str::from_utf8(bytes).unwrap();

    let mut result = CharTypeCount {
        hiragana: 0,
        katakana: 0,
    };

    for ch in s.chars() {
        match ch as u32 {
            0x3040..=0x309F => result.hiragana += 1,
            0x30A0..=0x30FF => result.katakana += 1,
            _ => {}
        }
    }

    // 構造体をメモリに割り当て、ポインタを返す
    let boxed = Box::new(result);
    let ptr = Box::into_raw(boxed) as i32;
    ptr
}

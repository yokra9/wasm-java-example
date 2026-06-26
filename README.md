# wasm-java-example

```bash
# Rust プロジェクトのビルド
cd rust
cargo build --target=wasm32-unknown-unknown --release

# Java プロジェクトのビルド
cd ../java
mvn clean install

# 実行
cd ../
cp rust/target/wasm32-unknown-unknown/release/count_kana.wasm .
cp java/target/wasm-java-example-1.0-SNAPSHOT-jar-with-dependencies.jar .

# Endive で実行
java -jar wasm-java-example-1.0-SNAPSHOT-jar-with-dependencies.jar # or
java -cp wasm-java-example-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.yokra9.Endive

# GraalWasm で実行
java -cp wasm-java-example-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.yokra9.Graal
```

# spring-security-sample

Springで認証を実装するとき、いつもリファレンスに困るので作った。  

* Spring: Boot, Web, Security
* UI: Thymeleaf
* DB: H2, JDBC
* Test: JUnit, Spring Security Test

## 構成
* ビルドツール: Gradle
* Spring Boot
* フロントエンド：Thymeleaf

## 認証のサンプル

* DBに認証情報を持たせてログイン
* Themeleafでユーザ名やロール名の表示
* 各ページのアクセス権限のテスト

## 認証情報
ユーザ名は以下の2つ。パスワードはいずれも`password`

* admin
* member

## 注意
ローカルで実行するときは`server.servlet.session.cookie.secure=true`があると動かないが、本番では付けること。

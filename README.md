# GraphQL Shop

Ứng dụng quản lý product/category sử dụng Spring Boot, GraphQL, Thymeleaf và SQL Server.

## Yêu cầu môi trường

- JDK 21+
- Maven 3.9+
- SQL Server

## Cấu hình nhanh

1. Tạo database `graphql_shop` trên SQL Server.
2. Sao chép `.env.example` thành `.env` và cập nhật thông tin đăng nhập.
3. Chạy file `src/main/resources/db/seed.sql` sau khi file này được bổ sung.
4. Khởi động ứng dụng bằng `mvn spring-boot:run`.

Các địa chỉ dự kiến:

- Home: `http://localhost:8080/`
- GraphQL: `http://localhost:8080/graphql`
- GraphiQL: `http://localhost:8080/graphiql`

## Cấu trúc triển khai

- Product/category domain và database schema
- GraphQL query/mutation
- Thymeleaf home page
- Thymeleaf trang CRUD, tìm kiếm và phân trang

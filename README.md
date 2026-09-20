# GraphQL Shop

Ứng dụng quản lý product/category sử dụng Spring Boot, GraphQL, Thymeleaf và SQL Server.

## Yêu cầu môi trường

- JDK 21+
- Maven 3.9+
- SQL Server

## Cấu hình nhanh

1. Tạo hoặc mở SQL Server instance trên máy.
2. Mở `src/main/resources/db/seed.sql` bằng SQL Server Management Studio và chạy toàn bộ file. Script tự tạo database `graphql_shop`, bảng, index và dữ liệu mẫu.
3. Sao chép `.env.example` thành `.env` và cập nhật thông tin đăng nhập SQL Server.
4. Nạp các biến trong `.env` vào môi trường chạy ứng dụng (IntelliJ/VS Code có thể dùng plugin dotenv), hoặc cấu hình trực tiếp các biến `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` trong Run Configuration.
5. Khởi động ứng dụng bằng `mvn spring-boot:run`.

PowerShell có thể nạp nhanh file `.env` cho phiên hiện tại:

```powershell
Get-Content .env | Where-Object { $_ -and -not $_.StartsWith('#') } | ForEach-Object {
    $name, $value = $_ -split '=', 2
    [Environment]::SetEnvironmentVariable($name, $value, 'Process')
}
mvn spring-boot:run
```

Các địa chỉ dự kiến:

- Home: `http://localhost:8080/`
- GraphQL: `http://localhost:8080/graphql`
- GraphiQL: `http://localhost:8080/graphiql`

## Chức năng

- Home hiển thị product theo giá tăng dần và lọc theo category.
- `/admin/products`: tìm kiếm, lọc category, phân trang, thêm/sửa/xóa product.
- `/admin/categories`: tìm kiếm, phân trang, thêm/sửa/xóa category.
- GraphQL cung cấp query/mutation tương ứng cho cả hai nhóm dữ liệu.

Ví dụ query product rẻ nhất trước:

```graphql
query {
  productsByPrice(page: 0, pageSize: 10) {
    items { id name price category { name } }
    pageInfo { totalElements totalPages }
  }
}
```

Ví dụ tạo product:

```graphql
mutation {
  createProduct(input: {
    name: "Desk Lamp"
    description: "Đèn bàn cho góc làm việc"
    price: 39.90
    stock: 20
    categoryId: "3"
  }) { id name price }
}
```

## Cấu trúc triển khai

- Product/category domain và database schema
- GraphQL query/mutation
- Thymeleaf home page
- Thymeleaf trang CRUD, tìm kiếm và phân trang

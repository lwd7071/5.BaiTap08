IF DB_ID(N'graphql_shop') IS NULL
BEGIN
    CREATE DATABASE graphql_shop;
END
GO

USE graphql_shop;
GO

IF OBJECT_ID(N'dbo.categories', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.categories (
        id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_categories PRIMARY KEY,
        name NVARCHAR(150) NOT NULL CONSTRAINT uq_categories_name UNIQUE,
        description NVARCHAR(500) NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT df_categories_created_at DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NOT NULL CONSTRAINT df_categories_updated_at DEFAULT SYSUTCDATETIME()
    );
END
GO

IF OBJECT_ID(N'dbo.products', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.products (
        id BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT pk_products PRIMARY KEY,
        name NVARCHAR(200) NOT NULL,
        description NVARCHAR(1000) NULL,
        price DECIMAL(18,2) NOT NULL CONSTRAINT ck_products_price CHECK (price >= 0),
        stock INT NOT NULL CONSTRAINT ck_products_stock CHECK (stock >= 0),
        image_url NVARCHAR(500) NULL,
        category_id BIGINT NOT NULL,
        created_at DATETIME2 NOT NULL CONSTRAINT df_products_created_at DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NOT NULL CONSTRAINT df_products_updated_at DEFAULT SYSUTCDATETIME(),
        CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES dbo.categories(id)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_products_price' AND object_id = OBJECT_ID(N'dbo.products'))
    CREATE INDEX idx_products_price ON dbo.products(price);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_products_name' AND object_id = OBJECT_ID(N'dbo.products'))
    CREATE INDEX idx_products_name ON dbo.products(name);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'idx_products_category_id' AND object_id = OBJECT_ID(N'dbo.products'))
    CREATE INDEX idx_products_category_id ON dbo.products(category_id);
GO

MERGE dbo.categories AS target
USING (VALUES
    (N'Laptop', N'Laptop và máy tính xách tay'),
    (N'Điện thoại', N'Điện thoại thông minh'),
    (N'Phụ kiện', N'Phụ kiện công nghệ'),
    (N'Màn hình', N'Màn hình làm việc và giải trí'),
    (N'Bàn phím', N'Bàn phím cơ và bàn phím văn phòng')
) AS source(name, description)
ON target.name = source.name
WHEN NOT MATCHED THEN
    INSERT (name, description) VALUES (source.name, source.description);
GO

MERGE dbo.products AS target
USING (
    SELECT v.name, v.description, v.price, v.stock, v.image_url, c.id AS category_id
    FROM (VALUES
        (N'Laptop Basic 14', N'Laptop học tập phổ thông', 899.00, 18, N'https://images.unsplash.com/photo-1496181133206-80ce9b88a853', N'Laptop'),
        (N'Laptop Pro 15', N'Laptop hiệu năng cao cho lập trình', 1599.00, 12, N'https://images.unsplash.com/photo-1517336714731-489689fd1ca8', N'Laptop'),
        (N'Ultrabook Air', N'Laptop mỏng nhẹ cho công việc', 1299.00, 9, N'https://images.unsplash.com/photo-1496181133206-80ce9b88a853', N'Laptop'),
        (N'Phone Mini', N'Điện thoại nhỏ gọn', 499.00, 25, N'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9', N'Điện thoại'),
        (N'Phone Plus', N'Điện thoại màn hình lớn', 799.00, 16, N'https://images.unsplash.com/photo-1598327105666-5b89351aff97', N'Điện thoại'),
        (N'Phone Ultra', N'Điện thoại cao cấp', 1199.00, 8, N'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9', N'Điện thoại'),
        (N'Wireless Mouse', N'Chuột không dây tiện dụng', 29.00, 80, N'https://images.unsplash.com/photo-1527814050087-3793815479db', N'Phụ kiện'),
        (N'USB-C Hub', N'Hub chuyển đổi đa năng', 59.00, 40, N'https://images.unsplash.com/photo-1625842268584-8f3296236761', N'Phụ kiện'),
        (N'Webcam HD', N'Webcam cho học tập và họp trực tuyến', 69.00, 30, N'https://images.unsplash.com/photo-1587825140708-dfaf72ae4b04', N'Phụ kiện'),
        (N'Monitor 24', N'Màn hình Full HD 24 inch', 179.00, 22, N'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf', N'Màn hình'),
        (N'Monitor 27 4K', N'Màn hình 4K 27 inch', 449.00, 10, N'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf', N'Màn hình'),
        (N'Mechanical Keyboard', N'Bàn phím cơ switch đỏ', 99.00, 35, N'https://images.unsplash.com/photo-1587829741301-dc798b83add3', N'Bàn phím')
    ) AS v(name, description, price, stock, image_url, category_name)
    JOIN dbo.categories c ON c.name = v.category_name
) AS source
ON target.name = source.name
WHEN NOT MATCHED THEN
    INSERT (name, description, price, stock, image_url, category_id)
    VALUES (source.name, source.description, source.price, source.stock, source.image_url, source.category_id);
GO

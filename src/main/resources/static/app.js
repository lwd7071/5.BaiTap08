const gql = async (query, variables = {}) => {
    const response = await fetch('/graphql', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query, variables })
    });
    const body = await response.json();
    if (!response.ok || body.errors) throw new Error(body.errors?.[0]?.message || 'Không thể kết nối GraphQL');
    return body.data;
};

const escapeHtml = value => String(value ?? '').replace(/[&<>'"]/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[char]));
const money = value => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'USD' }).format(value);
const toast = message => {
    const element = document.createElement('div');
    element.className = 'toast'; element.textContent = message; document.body.appendChild(element);
    setTimeout(() => element.remove(), 2800);
};
const productCard = product => `<article class="product-card">
    <div class="product-art">${product.imageUrl ? `<img src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.name)}" loading="lazy">` : `<span class="fallback">${escapeHtml(product.name.slice(0, 2).toUpperCase())}</span>`}</div>
    <div class="product-body"><div class="product-category">${escapeHtml(product.category?.name)}</div>
    <div class="product-name">${escapeHtml(product.name)}</div><div class="product-meta"><span class="price">${money(product.price)}</span><span class="stock">${product.stock} còn lại</span></div></div>
    </article>`;

async function initHome() {
    const products = document.querySelector('#home-products'); if (!products) return;
    const query = `query($categoryId: ID) { products(keyword: null, categoryId: $categoryId, page: 0, pageSize: 12, sortBy: PRICE, direction: ASC) { items { id name price stock imageUrl category { name } } pageInfo { totalElements } } }`;
    const categoryQuery = `query { categories(keyword: null, page: 0, pageSize: 50) { items { id name } } }`;
    try {
        const categories = await gql(categoryQuery); const strip = document.querySelector('#category-strip');
        strip.innerHTML = `<button class="chip active" data-category="">Tất cả sản phẩm</button>` + categories.categories.items.map(c => `<button class="chip" data-category="${c.id}">${escapeHtml(c.name)}</button>`).join('');
        strip.addEventListener('click', async event => { if (!event.target.matches('.chip')) return; strip.querySelectorAll('.chip').forEach(button => button.classList.remove('active')); event.target.classList.add('active'); await loadProducts(event.target.dataset.category || null); });
        await loadProducts(null);
    } catch (error) { products.innerHTML = `<div class="error">${escapeHtml(error.message)}</div>`; }
    async function loadProducts(categoryId) {
        products.innerHTML = '<div class="loading">Đang tải sản phẩm...</div>';
        try { const data = await gql(query, { categoryId }); const items = data.products.items; products.innerHTML = items.length ? items.map(productCard).join('') : '<div class="empty">Chưa có sản phẩm trong nhóm này.</div>'; }
        catch (error) { products.innerHTML = `<div class="error">${escapeHtml(error.message)}</div>`; }
    }
}

async function initProductAdmin() {
    const table = document.querySelector('#product-rows'); if (!table) return;
    let page = 0, keyword = '', editingId = null;
    const categorySelect = document.querySelector('#product-category-filter'); const formCategory = document.querySelector('#product-category');
    const categoryQuery = `query { categories(keyword: null, page: 0, pageSize: 100) { items { id name } } }`;
    const productQuery = `query($keyword: String, $categoryId: ID, $page: Int, $pageSize: Int) { products(keyword: $keyword, categoryId: $categoryId, page: $page, pageSize: $pageSize, sortBy: CREATED_AT, direction: DESC) { items { id name price stock category { id name } } pageInfo { page totalPages totalElements hasNext hasPrevious } } }`;
    const loadCategories = async () => { const data = await gql(categoryQuery); const options = data.categories.items.map(c => `<option value="${c.id}">${escapeHtml(c.name)}</option>`).join(''); categorySelect.innerHTML += options; formCategory.innerHTML = '<option value="">Chọn category</option>' + options; };
    const loadProducts = async () => { table.innerHTML = '<tr><td colspan="6">Đang tải...</td></tr>'; const data = await gql(productQuery, { keyword: keyword || null, categoryId: categorySelect.value || null, page, pageSize: 8 }); const pageInfo = data.products.pageInfo; table.innerHTML = data.products.items.length ? data.products.items.map(p => `<tr><td>#${p.id}</td><td><strong>${escapeHtml(p.name)}</strong></td><td>${escapeHtml(p.category.name)}</td><td>${money(p.price)}</td><td>${p.stock}</td><td><div class="actions"><button class="action-link" data-edit="${p.id}">Sửa</button><button class="action-link delete" data-delete="${p.id}">Xóa</button></div></td></tr>`).join('') : '<tr><td colspan="6">Không tìm thấy sản phẩm.</td></tr>'; document.querySelector('#page-label').textContent = `Trang ${pageInfo.page + 1} / ${Math.max(pageInfo.totalPages, 1)} · ${pageInfo.totalElements} sản phẩm`; document.querySelector('#prev-page').disabled = !pageInfo.hasPrevious; document.querySelector('#next-page').disabled = !pageInfo.hasNext; };
    const openForm = product => { editingId = product?.id || null; document.querySelector('#product-modal-title').textContent = editingId ? 'Sửa product' : 'Thêm product'; document.querySelector('#product-form').reset(); if (product) { for (const [key, value] of Object.entries(product)) { const field = document.querySelector(`#product-form [name="${key}"]`); if (field) field.value = value; } document.querySelector('#product-category').value = product.category.id; } document.querySelector('#product-modal').classList.remove('hidden'); };
    const closeForm = () => document.querySelector('#product-modal').classList.add('hidden');
    document.querySelector('#product-search').addEventListener('input', event => { keyword = event.target.value; page = 0; loadProducts(); }); document.querySelector('#product-category-filter').addEventListener('change', () => { page = 0; loadProducts(); }); document.querySelector('#prev-page').addEventListener('click', () => { page--; loadProducts(); }); document.querySelector('#next-page').addEventListener('click', () => { page++; loadProducts(); }); document.querySelector('#open-product-form').addEventListener('click', () => openForm()); document.querySelector('#close-product-form').addEventListener('click', closeForm); document.querySelector('#cancel-product-form').addEventListener('click', closeForm);
    table.addEventListener('click', async event => { const deleteId = event.target.dataset.delete; if (deleteId && confirm('Xóa product này?')) { await gql(`mutation($id: ID!) { deleteProduct(id: $id) }`, { id: deleteId }); toast('Đã xóa product'); loadProducts(); } const editId = event.target.dataset.edit; if (editId) { const data = await gql(`query($id: ID!) { productById(id: $id) { id name description price stock imageUrl category { id } } }`, { id: editId }); openForm(data.productById); } });
    document.querySelector('#product-form').addEventListener('submit', async event => { event.preventDefault(); const input = Object.fromEntries(new FormData(event.target)); input.price = Number(input.price); input.stock = Number(input.stock); input.categoryId = input.categoryId; const mutation = editingId ? `mutation($id: ID!, $input: ProductInput!) { updateProduct(id: $id, input: $input) { id } }` : `mutation($input: ProductInput!) { createProduct(input: $input) { id } }`; await gql(mutation, editingId ? { id: editingId, input } : { input }); closeForm(); toast(editingId ? 'Đã cập nhật product' : 'Đã thêm product'); loadProducts(); });
    try { await loadCategories(); await loadProducts(); } catch (error) { table.innerHTML = `<tr><td colspan="6" class="error">${escapeHtml(error.message)}</td></tr>`; }
}

async function initCategoryAdmin() {
    const table = document.querySelector('#category-rows'); if (!table) return;
    let page = 0, keyword = '', editingId = null;
    const query = `query($keyword: String, $page: Int, $pageSize: Int) { categories(keyword: $keyword, page: $page, pageSize: $pageSize) { items { id name description products { id } } pageInfo { page totalPages totalElements hasNext hasPrevious } } }`;
    const load = async () => { table.innerHTML = '<tr><td colspan="5">Đang tải...</td></tr>'; const data = await gql(query, { keyword: keyword || null, page, pageSize: 8 }); const pageInfo = data.categories.pageInfo; table.innerHTML = data.categories.items.length ? data.categories.items.map(c => `<tr><td>#${c.id}</td><td><strong>${escapeHtml(c.name)}</strong></td><td>${escapeHtml(c.description || '—')}</td><td>${c.products.length}</td><td><div class="actions"><button class="action-link" data-edit="${c.id}">Sửa</button><button class="action-link delete" data-delete="${c.id}">Xóa</button></div></td></tr>`).join('') : '<tr><td colspan="5">Không tìm thấy category.</td></tr>'; document.querySelector('#page-label').textContent = `Trang ${pageInfo.page + 1} / ${Math.max(pageInfo.totalPages, 1)} · ${pageInfo.totalElements} category`; document.querySelector('#prev-page').disabled = !pageInfo.hasPrevious; document.querySelector('#next-page').disabled = !pageInfo.hasNext; };
    const openForm = category => { editingId = category?.id || null; document.querySelector('#category-modal-title').textContent = editingId ? 'Sửa category' : 'Thêm category'; document.querySelector('#category-form').reset(); if (category) { document.querySelector('[name="name"]').value = category.name; document.querySelector('[name="description"]').value = category.description || ''; } document.querySelector('#category-modal').classList.remove('hidden'); };
    const closeForm = () => document.querySelector('#category-modal').classList.add('hidden');
    document.querySelector('#category-search').addEventListener('input', event => { keyword = event.target.value; page = 0; load(); }); document.querySelector('#prev-page').addEventListener('click', () => { page--; load(); }); document.querySelector('#next-page').addEventListener('click', () => { page++; load(); }); document.querySelector('#open-category-form').addEventListener('click', () => openForm()); document.querySelector('#close-category-form').addEventListener('click', closeForm); document.querySelector('#cancel-category-form').addEventListener('click', closeForm);
    table.addEventListener('click', async event => { const id = event.target.dataset.delete; if (id && confirm('Xóa category này?')) { await gql(`mutation($id: ID!) { deleteCategory(id: $id) }`, { id }); toast('Đã xóa category'); load(); } const editId = event.target.dataset.edit; if (editId) { const data = await gql(`query($id: ID!) { categoryById(id: $id) { id name description } }`, { id: editId }); openForm(data.categoryById); } });
    document.querySelector('#category-form').addEventListener('submit', async event => { event.preventDefault(); const input = Object.fromEntries(new FormData(event.target)); const mutation = editingId ? `mutation($id: ID!, $input: CategoryInput!) { updateCategory(id: $id, input: $input) { id } }` : `mutation($input: CategoryInput!) { createCategory(input: $input) { id } }`; await gql(mutation, editingId ? { id: editingId, input } : { input }); closeForm(); toast(editingId ? 'Đã cập nhật category' : 'Đã thêm category'); load(); });
    try { await load(); } catch (error) { table.innerHTML = `<tr><td colspan="5" class="error">${escapeHtml(error.message)}</td></tr>`; }
}

initHome(); initProductAdmin(); initCategoryAdmin();

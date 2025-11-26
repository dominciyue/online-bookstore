import apiClient from './apiClient'; // Import the new API client

const API_BASE_URL = 'http://localhost:8080/api'; // Your Spring Boot backend address
const BOOKS_API_URL = `${API_BASE_URL}/books`;

// Fetch all books, or by category, or by title keyword
const getAllBooks = async (params) => {
  // params can be an object like { category: 'fiction', title: 'Java' }
  let query = '';
  if (params) {
    const queryParams = new URLSearchParams();
    if (params.category) {
      queryParams.append('category', params.category);
    }
    if (params.title) {
      queryParams.append('title', params.title);
    }
    if (params.page !== undefined) {
      queryParams.append('page', params.page);
    }
    if (params.size !== undefined) {
      queryParams.append('size', params.size);
    }
    if (params.sort) {
      queryParams.append('sort', params.sort);
    }
    if (queryParams.toString()) {
      query = `?${queryParams.toString()}`;
    }
  }
  // Since /api/books is public, it doesn't strictly need the token via apiClient
  // but using apiClient consistently is fine.
  return apiClient.get(`${BOOKS_API_URL}${query}`);
};

// Fetch a single book by its ID
const getBookById = async (id) => {
  // Also public
  return apiClient.get(`${BOOKS_API_URL}/${id}`);
};

// Add a new book (requires admin or specific user roles typically)
const addBook = async (bookData) => {
  // This endpoint likely requires authentication, so apiClient will add the token.
  return apiClient.post(BOOKS_API_URL, bookData);
};

// Update an existing book
const updateBook = async (id, bookData) => {
  return apiClient.put(`${BOOKS_API_URL}/${id}`, bookData);
};

// Soft delete a book (Admin only)
const deleteBook = async (id) => {
  return apiClient.delete(`${BOOKS_API_URL}/${id}`);
};

// Hard delete a book (Admin only)
const hardDeleteBook = async (id) => {
  return apiClient.delete(`${BOOKS_API_URL}/${id}/hard`);
};

// Restore a deleted book (Admin only)
const restoreBook = async (id) => {
  return apiClient.post(`${BOOKS_API_URL}/${id}/restore`);
};

// Admin: Get all books including deleted ones
const getAllBooksForAdmin = async (params) => {
  let query = '';
  if (params) {
    const queryParams = new URLSearchParams();
    if (params.category) {
      queryParams.append('category', params.category);
    }
    if (params.title) {
      queryParams.append('title', params.title);
    }
    if (params.page !== undefined) {
      queryParams.append('page', params.page);
    }
    if (params.size !== undefined) {
      queryParams.append('size', params.size);
    }
    if (params.sort) {
      queryParams.append('sort', params.sort);
    }
    if (queryParams.toString()) {
      query = `?${queryParams.toString()}`;
    }
  }
  return apiClient.get(`${BOOKS_API_URL}/admin/all${query}`);
};

// Admin: Get only deleted books
const getDeletedBooks = async (params) => {
  let query = '';
  if (params) {
    const queryParams = new URLSearchParams();
    if (params.page !== undefined) {
      queryParams.append('page', params.page);
    }
    if (params.size !== undefined) {
      queryParams.append('size', params.size);
    }
    if (params.sort) {
      queryParams.append('sort', params.sort);
    }
    if (queryParams.toString()) {
      query = `?${queryParams.toString()}`;
    }
  }
  return apiClient.get(`${BOOKS_API_URL}/admin/deleted${query}`);
};

/**
 * 按标签搜索图书（核心功能）
 * @param {Array<string>} tags - 用户选择的标签数组
 * @param {Object} params - 分页参数 {page, size, sort}
 * @returns {Promise} 返回符合条件的图书列表
 */
const searchBooksByTags = async (tags, params = {}) => {
  const queryParams = new URLSearchParams();
  
  // 添加标签参数（多个标签）
  tags.forEach(tag => queryParams.append('tags', tag));
  
  // 添加分页参数
  if (params.page !== undefined) queryParams.append('page', params.page);
  if (params.size !== undefined) queryParams.append('size', params.size);
  if (params.sort) queryParams.append('sort', params.sort);
  
  return apiClient.get(`${BOOKS_API_URL}/search/by-tags?${queryParams.toString()}`);
};

const bookService = {
  getAllBooks,
  getBookById,
  addBook,
  updateBook,
  deleteBook,
  hardDeleteBook,
  restoreBook,
  getAllBooksForAdmin,
  getDeletedBooks,
  searchBooksByTags, // 新增
};

export default bookService;

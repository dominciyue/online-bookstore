/**
 * GraphQL Service
 * 提供 GraphQL 查询功能，用于按书名搜索书籍
 */

const GRAPHQL_ENDPOINT = 'http://localhost:8080/graphql';

/**
 * 发送 GraphQL 请求
 * @param {string} query - GraphQL 查询字符串
 * @param {object} variables - 查询变量
 * @returns {Promise<object>} - 查询结果
 */
async function executeGraphQLQuery(query, variables = {}) {
    try {
        const response = await fetch(GRAPHQL_ENDPOINT, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                query: query,
                variables: variables
            })
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();

        if (result.errors) {
            console.error('GraphQL Errors:', result.errors);
            throw new Error(result.errors[0].message);
        }

        return result.data;
    } catch (error) {
        console.error('GraphQL Query Error:', error);
        throw error;
    }
}

/**
 * 按书名搜索书籍（使用 GraphQL）
 * @param {string} title - 书籍名称
 * @param {number} page - 页码，默认0
 * @param {number} size - 每页大小，默认10
 * @returns {Promise<object>} - 包含书籍列表和分页信息的对象
 */
export async function searchBooksByTitleGraphQL(title, page = 0, size = 10) {
    // GraphQL 查询语句（使用变量）
    const query = `
        query SearchBooksByTitle($title: String!, $page: Int, $size: Int) {
            searchBooksByTitle(title: $title, page: $page, size: $size) {
                content {
                    id
                    title
                    author
                    isbn
                    publisher
                    price
                    cover
                    description
                    category
                    createdAt
                    updatedAt
                }
                totalElements
                totalPages
                number
                size
                numberOfElements
                first
                last
                empty
            }
        }
    `;

    // 查询变量
    const variables = {
        title: title,
        page: page,
        size: size
    };

    try {
        const data = await executeGraphQLQuery(query, variables);
        return data.searchBooksByTitle;
    } catch (error) {
        console.error('Search books by title failed:', error);
        throw error;
    }
}

/**
 * 根据ID获取书籍详情（使用 GraphQL）
 * @param {string|number} id - 书籍ID
 * @returns {Promise<object>} - 书籍详情对象
 */
export async function getBookByIdGraphQL(id) {
    const query = `
        query GetBookById($id: ID!) {
            getBookById(id: $id) {
                id
                title
                author
                isbn
                publisher
                price
                cover
                description
                category
                createdAt
                updatedAt
            }
        }
    `;

    const variables = {
        id: String(id)
    };

    try {
        const data = await executeGraphQLQuery(query, variables);
        return data.getBookById;
    } catch (error) {
        console.error('Get book by ID failed:', error);
        throw error;
    }
}

/**
 * 按书名搜索书籍（简化版，只返回基本信息）
 * @param {string} title - 书籍名称
 * @param {number} page - 页码
 * @param {number} size - 每页大小
 * @returns {Promise<object>} - 包含书籍列表和分页信息
 */
export async function searchBooksSimple(title, page = 0, size = 10) {
    const query = `
        query SearchBooks($title: String!, $page: Int, $size: Int) {
            searchBooksByTitle(title: $title, page: $page, size: $size) {
                content {
                    id
                    title
                    author
                    price
                    cover
                    category
                }
                totalElements
                totalPages
                number
            }
        }
    `;

    const variables = { title, page, size };

    try {
        const data = await executeGraphQLQuery(query, variables);
        return data.searchBooksByTitle;
    } catch (error) {
        console.error('Search books simple failed:', error);
        throw error;
    }
}

export default {
    searchBooksByTitleGraphQL,
    getBookByIdGraphQL,
    searchBooksSimple
};



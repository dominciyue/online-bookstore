import React, { useState } from 'react';
import { searchBooksByTitleGraphQL } from '../services/graphqlService';
import './GraphQLSearchPage.css';

/**
 * GraphQL 搜索页面
 * 演示如何使用 GraphQL 查询按书名搜索书籍
 */
function GraphQLSearchPage() {
    const [searchTitle, setSearchTitle] = useState('');
    const [searchResults, setSearchResults] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);
    const pageSize = 10;

    /**
     * 执行搜索
     */
    const handleSearch = async (page = 0) => {
        if (!searchTitle.trim()) {
            setError('请输入书籍名称');
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const result = await searchBooksByTitleGraphQL(searchTitle, page, pageSize);
            setSearchResults(result);
            setCurrentPage(page);
        } catch (err) {
            setError('搜索失败: ' + err.message);
            setSearchResults(null);
        } finally {
            setLoading(false);
        }
    };

    /**
     * 处理搜索按钮点击
     */
    const onSearchClick = () => {
        handleSearch(0);
    };

    /**
     * 处理回车键搜索
     */
    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            handleSearch(0);
        }
    };

    /**
     * 上一页
     */
    const handlePreviousPage = () => {
        if (currentPage > 0) {
            handleSearch(currentPage - 1);
        }
    };

    /**
     * 下一页
     */
    const handleNextPage = () => {
        if (searchResults && currentPage < searchResults.totalPages - 1) {
            handleSearch(currentPage + 1);
        }
    };

    return (
        <div className="graphql-search-page">
            <div className="search-container">
                <h1>GraphQL 书籍搜索</h1>
                <p className="description">
                    使用 GraphQL 查询按书名搜索书籍，支持变量和分页
                </p>

                <div className="search-box">
                    <input
                        type="text"
                        className="search-input"
                        placeholder="输入书籍名称（支持模糊搜索）"
                        value={searchTitle}
                        onChange={(e) => setSearchTitle(e.target.value)}
                        onKeyPress={handleKeyPress}
                        disabled={loading}
                    />
                    <button
                        className="search-button"
                        onClick={onSearchClick}
                        disabled={loading || !searchTitle.trim()}
                    >
                        {loading ? '搜索中...' : '搜索'}
                    </button>
                </div>

                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                {searchResults && (
                    <div className="results-container">
                        <div className="results-header">
                            <h2>搜索结果</h2>
                            <p className="results-info">
                                找到 {searchResults.totalElements} 本书籍，
                                共 {searchResults.totalPages} 页，
                                当前第 {searchResults.number + 1} 页
                            </p>
                        </div>

                        {searchResults.content.length === 0 ? (
                            <div className="no-results">
                                没有找到匹配的书籍
                            </div>
                        ) : (
                            <>
                                <div className="books-grid">
                                    {searchResults.content.map((book) => (
                                        <div key={book.id} className="book-card">
                                            <div className="book-cover">
                                                {book.cover ? (
                                                    <img src={book.cover} alt={book.title} />
                                                ) : (
                                                    <div className="no-cover">无封面</div>
                                                )}
                                            </div>
                                            <div className="book-info">
                                                <h3 className="book-title">{book.title}</h3>
                                                <p className="book-author">作者: {book.author || '未知'}</p>
                                                <p className="book-category">分类: {book.category || '未分类'}</p>
                                                <p className="book-price">
                                                    价格: ¥{book.price ? book.price.toFixed(2) : '0.00'}
                                                </p>
                                                {book.isbn && (
                                                    <p className="book-isbn">ISBN: {book.isbn}</p>
                                                )}
                                            </div>
                                        </div>
                                    ))}
                                </div>

                                {searchResults.totalPages > 1 && (
                                    <div className="pagination">
                                        <button
                                            onClick={handlePreviousPage}
                                            disabled={searchResults.first || loading}
                                            className="pagination-button"
                                        >
                                            上一页
                                        </button>
                                        <span className="pagination-info">
                                            第 {searchResults.number + 1} / {searchResults.totalPages} 页
                                        </span>
                                        <button
                                            onClick={handleNextPage}
                                            disabled={searchResults.last || loading}
                                            className="pagination-button"
                                        >
                                            下一页
                                        </button>
                                    </div>
                                )}
                            </>
                        )}
                    </div>
                )}

                <div className="graphql-info">
                    <h3>GraphQL 查询示例</h3>
                    <pre className="code-block">
{`query SearchBooksByTitle($title: String!, $page: Int, $size: Int) {
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
}`}
                    </pre>
                    <p className="info-text">
                        此页面使用 GraphQL 变量进行查询，可以复用同一个查询语句，
                        只需改变变量值即可实现不同的搜索。
                    </p>
                </div>
            </div>
        </div>
    );
}

export default GraphQLSearchPage;



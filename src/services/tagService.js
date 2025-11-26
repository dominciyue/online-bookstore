import apiClient from './apiClient';

const API_BASE_URL = 'http://localhost:8080/api';
const TAGS_API_URL = `${API_BASE_URL}/tags`;

/**
 * 标签服务
 * 用于与后端Neo4j标签图交互
 */

// 获取所有标签
const getAllTags = async () => {
  return apiClient.get(TAGS_API_URL);
};

// 获取标签树（用于前端显示分类树形结构）
const getTagTree = async () => {
  return apiClient.get(`${TAGS_API_URL}/tree`);
};

// 获取根标签（顶级分类）
const getRootTags = async () => {
  return apiClient.get(`${TAGS_API_URL}/roots`);
};

// 获取与指定标签相关的标签（2次边连接内）
const getRelatedTags = async (tag) => {
  return apiClient.get(`${TAGS_API_URL}/related?tag=${encodeURIComponent(tag)}`);
};

// 批量获取多个标签的相关标签（2次边连接内）
const getRelatedTagsBatch = async (tags) => {
  return apiClient.post(`${TAGS_API_URL}/related-batch`, tags);
};

// 获取指定标签的子标签
const getDescendants = async (tagName) => {
  return apiClient.get(`${TAGS_API_URL}/${encodeURIComponent(tagName)}/descendants`);
};

// 获取指定标签的父标签
const getAncestors = async (tagName) => {
  return apiClient.get(`${TAGS_API_URL}/${encodeURIComponent(tagName)}/ancestors`);
};

// 获取与指定标签直接相关的标签（1次边连接）
const getDirectlyRelatedTags = async (tagName) => {
  return apiClient.get(`${TAGS_API_URL}/${encodeURIComponent(tagName)}/directly-related`);
};

const tagService = {
  getAllTags,
  getTagTree,
  getRootTags,
  getRelatedTags,
  getRelatedTagsBatch,
  getDescendants,
  getAncestors,
  getDirectlyRelatedTags
};

export default tagService;


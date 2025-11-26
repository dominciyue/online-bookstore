import React, { useState, useEffect } from 'react';
import { Tree, Tag, Button, Space, Card, Spin, message } from 'antd';
import { SearchOutlined, CloseCircleOutlined } from '@ant-design/icons';
import tagService from '../services/tagService';

/**
 * 标签搜索面板组件
 * 提供树形标签选择和搜索功能
 */
const TagSearchPanel = ({ onSearch }) => {
  const [treeData, setTreeData] = useState([]);
  const [selectedTags, setSelectedTags] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchTagTree();
  }, []);

  // 获取标签树数据
  const fetchTagTree = async () => {
    setLoading(true);
    try {
      const rootTags = await tagService.getTagTree();
      const formattedTree = formatTreeData(rootTags);
      setTreeData(formattedTree);
    } catch (error) {
      console.error('获取标签树失败:', error);
      message.error('加载标签失败，请刷新重试');
    } finally {
      setLoading(false);
    }
  };

  // 递归格式化树形数据（适配Ant Design Tree组件）
  const formatTreeData = (nodes) => {
    if (!nodes || nodes.length === 0) return [];
    
    return nodes.map(node => ({
      title: node.description || node.name,
      key: node.name,
      children: node.subcategories && node.subcategories.length > 0
        ? formatTreeData(node.subcategories)
        : undefined
    }));
  };

  // 处理树节点选择
  const onSelect = (selectedKeys, info) => {
    if (selectedKeys.length > 0) {
      const newTag = selectedKeys[selectedKeys.length - 1];
      // 检查标签是否已选择
      if (!selectedTags.includes(newTag)) {
        setSelectedTags([...selectedTags, newTag]);
      } else {
        message.info(`标签"${newTag}"已选择`);
      }
    }
  };

  // 移除已选择的标签
  const removeTag = (tag) => {
    setSelectedTags(selectedTags.filter(t => t !== tag));
  };

  // 执行搜索
  const handleSearch = () => {
    if (selectedTags.length === 0) {
      message.warning('请至少选择一个标签');
      return;
    }
    onSearch(selectedTags);
  };

  // 清空所有选择
  const clearAll = () => {
    setSelectedTags([]);
  };

  return (
    <Card 
      title="按标签搜索" 
      style={{ marginBottom: 20 }}
      headStyle={{ backgroundColor: '#f0f2f5', fontWeight: 'bold' }}
    >
      {loading ? (
        <div style={{ textAlign: 'center', padding: '20px' }}>
          <Spin tip="加载标签中..." />
        </div>
      ) : (
        <>
          {/* 标签树 */}
          <div style={{ 
            maxHeight: '300px', 
            overflowY: 'auto', 
            marginBottom: 16,
            padding: '10px',
            border: '1px solid #f0f0f0',
            borderRadius: '4px'
          }}>
            <Tree
              treeData={treeData}
              onSelect={onSelect}
              showLine
              defaultExpandAll
            />
          </div>
          
          {/* 已选择的标签 */}
          <div style={{ marginBottom: 16 }}>
            <div style={{ 
              marginBottom: 8, 
              fontSize: '14px', 
              color: '#666',
              fontWeight: 500 
            }}>
              已选择的标签 ({selectedTags.length}):
            </div>
            <Space wrap>
              {selectedTags.length === 0 ? (
                <span style={{ color: '#999', fontSize: '13px' }}>
                  点击上方标签树进行选择
                </span>
              ) : (
                selectedTags.map(tag => (
                  <Tag
                    key={tag}
                    closable
                    onClose={() => removeTag(tag)}
                    color="blue"
                    style={{ fontSize: '13px', padding: '4px 8px' }}
                  >
                    {tag}
                  </Tag>
                ))
              )}
            </Space>
          </div>
          
          {/* 操作按钮 */}
          <div style={{ display: 'flex', gap: '8px' }}>
            <Button
              type="primary"
              icon={<SearchOutlined />}
              onClick={handleSearch}
              disabled={selectedTags.length === 0}
              style={{ flex: 1 }}
            >
              搜索图书
            </Button>
            <Button
              icon={<CloseCircleOutlined />}
              onClick={clearAll}
              disabled={selectedTags.length === 0}
            >
              清空
            </Button>
          </div>
          
          {/* 提示信息 */}
          {selectedTags.length > 0 && (
            <div style={{ 
              marginTop: 12, 
              padding: '8px 12px',
              backgroundColor: '#e6f7ff',
              border: '1px solid #91d5ff',
              borderRadius: '4px',
              fontSize: '12px',
              color: '#0050b3'
            }}>
              💡 提示：搜索将自动包含所选标签及其关联标签（2层关系内）的图书
            </div>
          )}
        </>
      )}
    </Card>
  );
};

export default TagSearchPanel;


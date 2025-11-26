// MongoDB数据验证脚本
// 在MongoDB Compass的Shell中执行，或使用 mongosh 命令行执行

// 使用bookstore_mongo数据库
use('bookstore_mongo');

// 1. 检查books集合是否存在
console.log("=== 1. 检查集合 ===");
const collections = db.getCollectionNames();
console.log("数据库中的集合:", collections);

if (!collections.includes('books')) {
    console.log("❌ 错误：books集合不存在！");
    console.log("请确保已导入books_mongo_import.json");
} else {
    console.log("✓ books集合存在");
}

// 2. 统计文档数量
console.log("\n=== 2. 统计文档数量 ===");
const count = db.books.countDocuments();
console.log(`books集合中有 ${count} 个文档`);

if (count === 0) {
    console.log("❌ 错误：books集合为空！");
    console.log("请导入books_mongo_import.json文件");
}

// 3. 查看示例文档
console.log("\n=== 3. 查看示例文档 ===");
const sampleDoc = db.books.findOne();
if (sampleDoc) {
    console.log("示例文档结构:");
    console.log(JSON.stringify(sampleDoc, null, 2));
    
    // 检查必要字段
    console.log("\n字段检查:");
    console.log("✓ _id:", sampleDoc._id ? "存在" : "缺失");
    console.log("✓ bookId:", sampleDoc.bookId ? "存在" : "缺失");
    console.log("✓ description:", sampleDoc.description ? "存在" : "缺失");
    console.log("✓ reviews:", sampleDoc.reviews ? "存在" : "缺失");
}

// 4. 检查bookId索引
console.log("\n=== 4. 检查索引 ===");
const indexes = db.books.getIndexes();
console.log("当前索引:");
indexes.forEach((index, i) => {
    console.log(`索引${i + 1}:`, JSON.stringify(index.key));
});

const hasBookIdIndex = indexes.some(idx => idx.key.bookId !== undefined);
if (!hasBookIdIndex) {
    console.log("⚠ 警告：bookId字段没有索引，查询性能可能较低");
    console.log("建议创建索引：db.books.createIndex({ bookId: 1 }, { unique: true })");
}

// 5. 查询特定bookId的文档
console.log("\n=== 5. 测试查询功能 ===");
const testBookId = 2; // 测试bookId=2的书籍
const testDoc = db.books.findOne({ bookId: testBookId });

if (testDoc) {
    console.log(`✓ 成功查询到 bookId=${testBookId} 的文档`);
    console.log("书籍描述:", testDoc.description);
} else {
    console.log(`❌ 未找到 bookId=${testBookId} 的文档`);
}

// 6. 检查所有文档的description字段
console.log("\n=== 6. 检查description字段 ===");
const docsWithoutDesc = db.books.countDocuments({ 
    $or: [
        { description: { $exists: false } },
        { description: null },
        { description: "" }
    ]
});

console.log(`有 ${docsWithoutDesc} 个文档缺少有效的description`);

if (docsWithoutDesc > 0) {
    console.log("⚠ 警告：部分文档没有description，这些书籍在前端可能显示为空");
    const exampleMissing = db.books.find({ 
        $or: [
            { description: { $exists: false } },
            { description: null },
            { description: "" }
        ]
    }).limit(5);
    
    console.log("缺少description的文档示例:");
    exampleMissing.forEach(doc => {
        console.log(`  bookId: ${doc.bookId}, description: ${doc.description}`);
    });
}

// 7. 列出所有bookId
console.log("\n=== 7. 数据库中的所有bookId ===");
const allBookIds = db.books.find({}, { bookId: 1, _id: 0 }).sort({ bookId: 1 });
const bookIdList = [];
allBookIds.forEach(doc => {
    bookIdList.push(doc.bookId);
});
console.log("bookId列表:", bookIdList.join(', '));

console.log("\n=== 验证完成 ===");
console.log(`✓ 数据库: bookstore_mongo`);
console.log(`✓ 集合: books`);
console.log(`✓ 文档数量: ${count}`);
console.log(`✓ bookId范围: ${Math.min(...bookIdList)} - ${Math.max(...bookIdList)}`);


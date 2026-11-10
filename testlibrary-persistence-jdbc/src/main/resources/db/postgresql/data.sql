INSERT INTO booking_item (item_id, title, preview_content, status)
SELECT 1, 'Java 编程思想',
       '本书从编程思想出发，系统讲解 Java 语言的核心概念、面向对象设计、集合、并发与 JVM 基础。适合希望打牢 Java 基础并理解语言设计哲学的读者。',
       'AVAILABLE'
WHERE NOT EXISTS (SELECT 1 FROM booking_item WHERE title = 'Java 编程思想');

INSERT INTO booking_item (item_id, title, preview_content, status)
SELECT 2, '设计模式',
       '通过经典设计模式讲解如何组织可复用、可扩展的面向对象代码。书中结合大量示例说明创建型、结构型和行为型模式的适用场景，是提升代码设计能力的常备参考。',
       'AVAILABLE'
WHERE NOT EXISTS (SELECT 1 FROM booking_item WHERE title = '设计模式');

INSERT INTO booking_item (item_id, title, preview_content, status)
SELECT 3, '重构：改善既有代码的设计',
       '围绕如何在不改变外部行为的前提下改善代码结构展开，介绍重构手法、代码坏味道与测试驱动的小步改进方式，适合有经验的开发者持续优化代码质量。',
       'AVAILABLE'
WHERE NOT EXISTS (SELECT 1 FROM booking_item WHERE title = '重构：改善既有代码的设计');
UPDATE booking_item
SET preview_content = '本书从编程思想出发，系统讲解 Java 语言的核心概念、面向对象设计、集合、并发与 JVM 基础。适合希望打牢 Java 基础并理解语言设计哲学的读者。'
WHERE title = 'Java 编程思想' AND (preview_content IS NULL OR preview_content = '');

UPDATE booking_item
SET preview_content = '通过经典设计模式讲解如何组织可复用、可扩展的面向对象代码。书中结合大量示例说明创建型、结构型和行为型模式的适用场景，是提升代码设计能力的常备参考。'
WHERE title = '设计模式' AND (preview_content IS NULL OR preview_content = '');

UPDATE booking_item
SET preview_content = '围绕如何在不改变外部行为的前提下改善代码结构展开，介绍重构手法、代码坏味道与测试驱动的小步改进方式，适合有经验的开发者持续优化代码质量。'
WHERE title = '重构：改善既有代码的设计' AND (preview_content IS NULL OR preview_content = '');
UPDATE booking_item
SET preview_content = '暂无预览内容。'
WHERE preview_content IS NULL OR preview_content = '';

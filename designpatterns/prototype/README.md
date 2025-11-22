# 原型模式示例

## 📁 文件说明

- `PrototypeExample.java` - 原型模式完整示例代码
- `README.md` - 本文件

## 🎯 学习目标

通过本示例，你将学会：
1. 什么是原型模式，为什么要用它
2. 浅克隆和深克隆的区别
3. 如何在 Java 中实现原型模式
4. 原型模式在实际项目中的应用

## 🚀 快速开始

### 编译运行

```bash
# 进入目录
cd c:\android\inter\inter\designpatterns\prototype

# 编译（指定 UTF-8 编码）
javac -encoding UTF-8 PrototypeExample.java

# 运行
java PrototypeExample
```

### 在 IDE 中运行

1. 用 IntelliJ IDEA 或 Eclipse 打开 `PrototypeExample.java`
2. 右键点击文件，选择 "Run"
3. 查看控制台输出

## 📚 代码结构

### 1. Weapon 类（武器）
```java
class Weapon implements Cloneable {
    private String name;
    private int damage;
    
    @Override
    public Weapon clone() {
        // 实现克隆逻辑
    }
}
```

### 2. Monster 类（浅克隆怪物）
```java
class Monster implements Cloneable {
    private String name;      // 基本类型
    private int health;       // 基本类型
    private Weapon weapon;    // 引用类型（浅克隆时只复制引用）
    
    @Override
    public Monster clone() {
        return (Monster) super.clone();  // 浅克隆
    }
}
```

### 3. DeepMonster 类（深克隆怪物）
```java
class DeepMonster implements Cloneable {
    private String name;
    private int health;
    private Weapon weapon;    // 引用类型（深克隆时会复制对象）
    
    @Override
    public DeepMonster clone() {
        DeepMonster cloned = (DeepMonster) super.clone();
        cloned.weapon = this.weapon.clone();  // 深克隆引用字段
        return cloned;
    }
}
```

## 🔍 示例演示

### 测试1：浅克隆

```java
// 创建原型
Weapon sword = new Weapon("铁剑", 50);
Monster prototype = new Monster("哥布林", 100, sword);

// 克隆
Monster clone1 = prototype.clone();
Monster clone2 = prototype.clone();

// 修改原型的武器
sword.setDamage(100);

// 结果：所有克隆对象的武器都改变了！
// 原因：浅克隆只复制引用，所有对象的 weapon 指向同一个对象
```

**浅克隆的特点**：
- ✅ 基本类型字段（name、health）会被复制
- ❌ 引用类型字段（weapon）只复制引用，不复制对象
- ⚠️ 修改原型的引用字段会影响所有克隆对象

### 测试2：深克隆

```java
// 创建原型
Weapon axe = new Weapon("战斧", 80);
DeepMonster prototype = new DeepMonster("兽人", 200, axe);

// 深克隆
DeepMonster clone1 = prototype.clone();
DeepMonster clone2 = prototype.clone();

// 修改原型的武器
axe.setDamage(120);

// 结果：克隆对象的武器不受影响！
// 原因：深克隆创建了完全独立的对象
```

**深克隆的特点**：
- ✅ 基本类型字段会被复制
- ✅ 引用类型字段也会被复制（创建新对象）
- ✅ 克隆对象和原型完全独立，互不影响

## 💡 实际应用场景

### 1. 游戏开发
```java
// 创建怪物模板
DeepMonster template = new DeepMonster("哥布林", 100, standardWeapon);

// 快速创建 100 个相似的怪物
for (int i = 0; i < 100; i++) {
    DeepMonster monster = template.clone();
    monster.setName("哥布林#" + i);
    monster.setPosition(randomX(), randomY());
}
```

### 2. 配置对象复制
```java
// 复制配置用于不同环境
AppConfig devConfig = new AppConfig("dev", "http://dev.api.com");
AppConfig prodConfig = devConfig.clone();
prodConfig.setEnvironment("prod");
prodConfig.setApiUrl("http://api.com");
```

### 3. 编辑功能
```java
// 克隆数据用于编辑，取消时不影响原数据
User originalUser = getUser();
User editingUser = originalUser.clone();

// 用户编辑 editingUser
// 保存时才更新 originalUser
```

## 📊 浅克隆 vs 深克隆对比

| 特性 | 浅克隆 | 深克隆 |
|------|--------|--------|
| **基本类型** | 复制值 | 复制值 |
| **引用类型** | 复制引用 | 复制对象 |
| **实现难度** | 简单 | 复杂 |
| **性能** | 快 | 慢 |
| **独立性** | 部分独立 | 完全独立 |
| **使用场景** | 引用字段不会被修改 | 需要完全独立的副本 |

## ⚠️ 注意事项

1. **实现 Cloneable 接口**
   - 必须实现 `Cloneable` 接口，否则会抛出 `CloneNotSupportedException`

2. **重写 clone() 方法**
   - 必须重写 `clone()` 方法，并调用 `super.clone()`

3. **深克隆的递归**
   - 深克隆时，所有引用类型字段也要实现 `Cloneable` 并重写 `clone()`

4. **性能考虑**
   - 浅克隆性能好，但要注意引用共享问题
   - 深克隆性能较差，但对象完全独立

## 🎓 学习建议

1. **运行代码**：先运行示例，观察输出结果
2. **修改代码**：尝试添加新字段（如 `List`、`Map`），实现深克隆
3. **对比测试**：修改浅克隆和深克隆的对象，观察区别
4. **实际应用**：在自己的项目中尝试使用原型模式

## 📝 练习题

1. 为 `Monster` 类添加一个 `List<Skill>` 字段，实现深克隆
2. 创建一个 `Character` 类（游戏角色），包含装备、技能等，实现深克隆
3. 实现一个简单的撤销/重做功能（使用原型模式保存历史状态）

## 🔗 相关资源

- [原型模式.md](../原型模式.md) - 详细的原型模式教程
- [责任链.md](../责任链.md) - 责任链模式教程
- [observer/ObserverExample.java](../observer/ObserverExample.java) - 观察者模式示例
- [chain/ChainExample.java](../chain/com/inter/designpatterns/chain/ChainExample.java) - 责任链模式示例

## 🎉 完成标志

当你能够：
- ✅ 解释浅克隆和深克隆的区别
- ✅ 独立实现一个深克隆的类
- ✅ 知道什么时候该用原型模式
- ✅ 能在实际项目中应用原型模式

恭喜你，已经掌握了原型模式！🎊


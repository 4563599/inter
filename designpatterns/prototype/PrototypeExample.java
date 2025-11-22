/**
 * 原型模式示例 - 游戏怪物克隆系统
 * 
 * 学习目标：
 * 1. 理解原型模式的核心思想：通过克隆创建对象，而不是 new
 * 2. 掌握浅克隆和深克隆的区别
 * 3. 学会在实际项目中应用原型模式
 * 
 * 运行方式：
 * javac PrototypeExample.java
 * java PrototypeExample
 */

// ============================================================
// 第一步：创建需要被克隆的类（武器类）
// ============================================================

/**
 * 武器类 - 怪物的装备
 * 实现 Cloneable 接口，表示这个类的对象可以被克隆
 */
class Weapon implements Cloneable {
    private String name;      // 武器名称
    private int damage;       // 伤害值
    
    // 构造函数：创建武器时需要指定名称和伤害
    public Weapon(String name, int damage) {
        this.name = name;
        this.damage = damage;
        System.out.println("    [创建武器] " + name + "，伤害：" + damage);
    }
    
    // 克隆方法：创建当前武器的副本
    @Override
    public Weapon clone() {
        try {
            // 调用 Object 的 clone() 方法，实现浅克隆
            Weapon cloned = (Weapon) super.clone();
            System.out.println("    [克隆武器] " + this.name);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("武器克隆失败", e);
        }
    }
    
    // getter 和 setter 方法
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public void setDamage(int damage) {
        this.damage = damage;
    }
    
    @Override
    public String toString() {
        return name + "(伤害:" + damage + ")";
    }
}

// ============================================================
// 第二步：创建怪物类（浅克隆版本）
// ============================================================

/**
 * 怪物类 - 浅克隆版本
 * 
 * 浅克隆特点：
 * - 基本类型字段（如 name、health）会被复制
 * - 引用类型字段（如 weapon）只复制引用，不复制对象本身
 * - 结果：克隆对象和原对象的 weapon 指向同一个武器对象
 */
class Monster implements Cloneable {
    private String name;      // 怪物名称（基本类型，会被复制）
    private int health;       // 生命值（基本类型，会被复制）
    private Weapon weapon;    // 武器（引用类型，浅克隆时只复制引用）
    
    // 构造函数：创建怪物
    public Monster(String name, int health, Weapon weapon) {
        this.name = name;
        this.health = health;
        this.weapon = weapon;
        System.out.println("[创建怪物] " + name + "，生命：" + health + "，武器：" + weapon);
    }
    
    // 克隆方法：实现浅克隆
    @Override
    public Monster clone() {
        try {
            // super.clone() 会复制所有字段
            // 但对于引用类型字段（weapon），只复制引用，不复制对象
            Monster cloned = (Monster) super.clone();
            System.out.println("[浅克隆怪物] " + this.name + " -> " + cloned.name);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("怪物克隆失败", e);
        }
    }
    
    // getter 和 setter 方法
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }
    
    public Weapon getWeapon() {
        return weapon;
    }
    
    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }
    
    @Override
    public String toString() {
        return "怪物{名称='" + name + "', 生命=" + health + ", 武器=" + weapon + "}";
    }
}

// ============================================================
// 第三步：创建深克隆版本的怪物类
// ============================================================

/**
 * 深克隆怪物类
 * 
 * 深克隆特点：
 * - 基本类型字段会被复制
 * - 引用类型字段也会被复制（创建新对象）
 * - 结果：克隆对象和原对象完全独立，互不影响
 */
class DeepMonster implements Cloneable {
    private String name;
    private int health;
    private Weapon weapon;
    
    public DeepMonster(String name, int health, Weapon weapon) {
        this.name = name;
        this.health = health;
        this.weapon = weapon;
        System.out.println("[创建深克隆怪物] " + name + "，生命：" + health + "，武器：" + weapon);
    }
    
    // 深克隆方法：手动克隆所有引用类型字段
    @Override
    public DeepMonster clone() {
        try {
            // 先调用 super.clone() 进行浅克隆
            DeepMonster cloned = (DeepMonster) super.clone();
            
            // 手动克隆引用类型字段，实现深克隆
            // 这样克隆对象就有了自己独立的 weapon 对象
            if (this.weapon != null) {
                cloned.weapon = this.weapon.clone();
            }
            
            System.out.println("[深克隆怪物] " + this.name + " -> " + cloned.name + "（武器也被克隆）");
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("深克隆失败", e);
        }
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }
    
    public Weapon getWeapon() {
        return weapon;
    }
    
    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }
    
    @Override
    public String toString() {
        return "深克隆怪物{名称='" + name + "', 生命=" + health + ", 武器=" + weapon + "}";
    }
}

// ============================================================
// 第四步：测试原型模式
// ============================================================

public class PrototypeExample {
    
    public static void main(String[] args) {
        printLine("=", 60);
        System.out.println("原型模式示例 - 游戏怪物克隆系统");
        printLine("=", 60);
        
        // 测试浅克隆
        testShallowClone();
        
        System.out.println();
        
        // 测试深克隆
        testDeepClone();
        
        System.out.println();
        
        // 实际应用场景
        practicalExample();
    }
    
    /**
     * 测试浅克隆
     * 
     * 浅克隆的问题：
     * - 修改克隆对象的基本类型字段（name、health）不影响原对象 ✓
     * - 修改克隆对象的引用类型字段（weapon）会影响原对象 ✗
     */
    private static void testShallowClone() {
        System.out.println("\n【测试1：浅克隆】");
        printLine("-", 60);
        
        // 1. 创建原型对象（模板怪物）
        System.out.println("\n步骤1：创建原型怪物");
        Weapon sword = new Weapon("铁剑", 50);
        Monster prototype = new Monster("哥布林", 100, sword);
        
        // 2. 通过克隆创建新怪物
        System.out.println("\n步骤2：克隆怪物");
        Monster clone1 = prototype.clone();
        Monster clone2 = prototype.clone();
        
        // 3. 修改克隆对象的基本类型字段
        System.out.println("\n步骤3：修改克隆对象的名称和生命值");
        clone1.setName("哥布林战士");
        clone1.setHealth(150);
        
        clone2.setName("哥布林法师");
        clone2.setHealth(80);
        
        // 4. 打印结果：基本类型字段互不影响
        System.out.println("\n步骤4：查看结果（基本类型字段）");
        System.out.println("原型: " + prototype);
        System.out.println("克隆1: " + clone1);
        System.out.println("克隆2: " + clone2);
        System.out.println("✓ 结论：修改克隆对象的名称和生命值，不影响原型");
        
        // 5. 修改原型的武器（引用类型字段）
        System.out.println("\n步骤5：修改原型的武器伤害");
        System.out.println("将武器伤害从 50 改为 100");
        sword.setDamage(100);
        
        // 6. 打印结果：引用类型字段会相互影响
        System.out.println("\n步骤6：查看结果（引用类型字段）");
        System.out.println("原型: " + prototype);
        System.out.println("克隆1: " + clone1);
        System.out.println("克隆2: " + clone2);
        System.out.println("✗ 问题：修改原型的武器，所有克隆对象的武器都改变了！");
        System.out.println("原因：浅克隆只复制引用，所有对象的 weapon 指向同一个对象");
    }
    
    /**
     * 测试深克隆
     * 
     * 深克隆的优点：
     * - 克隆对象和原对象完全独立
     * - 修改任何字段都不会相互影响
     */
    private static void testDeepClone() {
        System.out.println("\n【测试2：深克隆】");
        printLine("-", 60);
        
        // 1. 创建原型对象
        System.out.println("\n步骤1：创建原型怪物");
        Weapon axe = new Weapon("战斧", 80);
        DeepMonster prototype = new DeepMonster("兽人", 200, axe);
        
        // 2. 通过深克隆创建新怪物
        System.out.println("\n步骤2：深克隆怪物");
        DeepMonster clone1 = prototype.clone();
        DeepMonster clone2 = prototype.clone();
        
        // 3. 修改克隆对象的属性
        System.out.println("\n步骤3：修改克隆对象的属性");
        clone1.setName("兽人战士");
        clone1.setHealth(250);
        clone1.getWeapon().setDamage(100);  // 修改武器伤害
        
        clone2.setName("兽人萨满");
        clone2.setHealth(150);
        clone2.getWeapon().setDamage(60);
        
        // 4. 修改原型的武器
        System.out.println("\n步骤4：修改原型的武器伤害");
        System.out.println("将原型武器伤害从 80 改为 120");
        axe.setDamage(120);
        
        // 5. 打印结果：完全独立，互不影响
        System.out.println("\n步骤5：查看结果");
        System.out.println("原型: " + prototype);
        System.out.println("克隆1: " + clone1);
        System.out.println("克隆2: " + clone2);
        System.out.println("✓ 结论：深克隆创建了完全独立的对象，互不影响！");
    }
    
    /**
     * 实际应用场景：快速创建大量相似的游戏对象
     * 
     * 场景：游戏中需要生成 100 个哥布林，它们有相同的基础属性，
     *      但位置和生命值略有不同
     */
    private static void practicalExample() {
        System.out.println("\n【实际应用：批量创建游戏怪物】");
        printLine("-", 60);
        
        // 1. 创建怪物模板
        System.out.println("\n步骤1：创建怪物模板");
        Weapon standardWeapon = new Weapon("标准武器", 30);
        DeepMonster template = new DeepMonster("哥布林", 100, standardWeapon);
        
        // 2. 使用原型模式快速创建 10 个怪物（实际游戏中可能是 100、1000 个）
        System.out.println("\n步骤2：使用模板快速创建 10 个怪物");
        System.out.println("（如果不用原型模式，需要 new 10 次，还要重复设置属性）");
        
        for (int i = 1; i <= 10; i++) {
            // 克隆模板，快速创建新怪物
            DeepMonster monster = template.clone();
            
            // 只需要修改少量属性（如位置、生命值）
            monster.setName("哥布林#" + i);
            monster.setHealth(100 + i * 10);  // 生命值略有不同
            
            // 模拟：设置怪物的位置（实际游戏中会有 x, y 坐标）
            // monster.setPosition(randomX(), randomY());
            
            if (i <= 3) {  // 只打印前 3 个，避免输出太多
                System.out.println("  创建: " + monster);
            }
        }
        
        System.out.println("  ... 省略其他 7 个怪物");
        
        System.out.println("\n✓ 优势：");
        System.out.println("  1. 不需要重复调用构造函数");
        System.out.println("  2. 不需要重复设置相同的属性");
        System.out.println("  3. 创建速度快，性能好");
        System.out.println("  4. 代码简洁，易于维护");
        
        System.out.println();
        printLine("=", 60);
        System.out.println("学习总结：");
        System.out.println("1. 浅克隆：快速，但引用类型字段会共享");
        System.out.println("2. 深克隆：完全独立，但需要手动克隆引用字段");
        System.out.println("3. 使用场景：需要创建大量相似对象时，用原型模式可以提高性能");
        printLine("=", 60);
    }
    
    // 辅助方法：打印分隔线
    private static void printLine(String ch, int count) {
        for (int i = 0; i < count; i++) {
            System.out.print(ch);
        }
        System.out.println();
    }
}


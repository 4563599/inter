package com.inter.designpatterns.chain;

/**
 * 责任链模式的 "请假审批" 示例
 * 一个请求在处理者链条中传递，直到有一个处理者处理它为止。
 */

// --- 第0步：定义请求对象 ---
// 这就是我们的"请假条"
class LeaveRequest {
    private String name;
    private int days;

    public LeaveRequest(String name, int days) {
        this.name = name;
        this.days = days;
    }

    public String getName() {
        return name;
    }

    public int getDays() {
        return days;
    }
}


// --- 第1步：定义“处理器”的统一标准 (抽象Handler) ---
// 定义所有审批人（处理器）的共同行为
abstract class Handler {
    protected Handler nextHandler; // 持有下一个处理器的引用

    // 设置下一个处理器
    public void setNext(Handler nextHandler) {
        this.nextHandler = nextHandler;
    }

    // 处理请求的抽象方法
    public abstract void handleRequest(LeaveRequest request);
}

// --- 第2步：创建具体的“处理器”们 ---

// 具体的处理器1：班主任
class ClassTeacher extends Handler {
    @Override
    public void handleRequest(LeaveRequest request) {
        if (request.getDays() <= 1) {
            System.out.println("学生 " + request.getName() + " 请假 " + request.getDays() + " 天，【班主任】审批通过。");
        } else {
            System.out.println("班主任处理不了，转交给年级主任。");
            if (nextHandler != null) {
                nextHandler.handleRequest(request);
            }
        }
    }
}

// 具体的处理器2：年级主任
class GradeDirector extends Handler {
    @Override
    public void handleRequest(LeaveRequest request) {
        if (request.getDays() > 1 && request.getDays() <= 7) {
            System.out.println("学生 " + request.getName() + " 请假 " + request.getDays() + " 天，【年级主任】审批通过。");
        } else {
            System.out.println("年级主任处理不了，转交给校长。");
            if (nextHandler != null) {
                nextHandler.handleRequest(request);
            }
        }
    }
}

// 具体的处理器3：校长
class Principal extends Handler {
    @Override
    public void handleRequest(LeaveRequest request) {
        // 校长是链条的最后，他能处理所有剩下的情况
        if (request.getDays() > 7) {
            System.out.println("学生 " + request.getName() + " 请假 " + request.getDays() + " 天，【校长】审批通过。");
        }
    }
}


// --- 第3步：把“处理器”们串成“链条”并使用 ---
public class ChainExample {
    public static void main(String[] args) {
        // 1. 创建处理器实例
        Handler teacher = new ClassTeacher();
        Handler director = new GradeDirector();
        Handler principal = new Principal();

        // 2. 组装责任链 (设置好上下级关系)
        teacher.setNext(director);
        director.setNext(principal);

        // 3. 提交请求给链条的开端（班主任）
        System.out.println("--- 小明请假1天 ---");
        teacher.handleRequest(new LeaveRequest("小明", 1));

        System.out.println("\n--- 小红请假5天 ---");
        teacher.handleRequest(new LeaveRequest("小红", 5));

        System.out.println("\n--- 小强请假10天 ---");
        teacher.handleRequest(new LeaveRequest("小强", 10));
    }
}

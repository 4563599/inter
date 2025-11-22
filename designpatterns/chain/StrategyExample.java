/**
 * 策略模式示例
 */
public class StrategyExample {
    
    public static void main(String[] args) {
        // 测试不同的支付策略
        PaymentContext context = new PaymentContext();
        
        // 使用支付宝支付
        context.setStrategy(new AlipayStrategy());
        context.pay(100);
        
        // 使用微信支付
        context.setStrategy(new WechatStrategy());
        context.pay(200);
        
        // 使用信用卡支付
        context.setStrategy(new CreditCardStrategy());
        context.pay(300);
    }
}

// 策略接口
interface PaymentStrategy {
    void pay(int amount);
}

// 支付宝策略
class AlipayStrategy implements PaymentStrategy {
    @Override
    public void pay(int amount) {
        System.out.println("使用支付宝支付: " + amount + " 元");
    }
}

// 微信策略
class WechatStrategy implements PaymentStrategy {
    @Override
    public void pay(int amount) {
        System.out.println("使用微信支付: " + amount + " 元");
    }
}

// 信用卡策略
class CreditCardStrategy implements PaymentStrategy {
    @Override
    public void pay(int amount) {
        System.out.println("使用信用卡支付: " + amount + " 元");
    }
}

// 支付上下文
class PaymentContext {
    private PaymentStrategy strategy;
    
    public void setStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void pay(int amount) {
        if (strategy == null) {
            System.out.println("请先设置支付方式");
            return;
        }
        strategy.pay(amount);
    }
}


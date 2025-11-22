/**
 * 观察者模式示例
 */
public class ObserverExample {
    
    public static void main(String[] args) {
        // 创建主题（被观察者）
        WeatherStation station = new WeatherStation();
        
        // 创建观察者
        PhoneDisplay phone = new PhoneDisplay();
        TVDisplay tv = new TVDisplay();
        
        // 注册观察者
        station.addObserver(phone);
        station.addObserver(tv);
        
        // 天气变化，通知所有观察者
        System.out.println("=== 第一次天气更新 ===");
        station.setTemperature(25);
        
        System.out.println("\n=== 第二次天气更新 ===");
        station.setTemperature(30);
    }
}

// 观察者接口
interface Observer {
    void update(int temperature);
}

// 主题（被观察者）
class WeatherStation {
    private java.util.List<Observer> observers = new java.util.ArrayList<>();
    private int temperature;
    
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    
    public void setTemperature(int temp) {
        this.temperature = temp;
        notifyObservers();
    }
    
    private void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(temperature);
        }
    }
}

// 手机显示屏（观察者）
class PhoneDisplay implements Observer {
    @Override
    public void update(int temperature) {
        System.out.println("手机显示: 当前温度 " + temperature + "°C");
    }
}

// 电视显示屏（观察者）
class TVDisplay implements Observer {
    @Override
    public void update(int temperature) {
        System.out.println("电视显示: 温度更新为 " + temperature + "°C");
    }
}


/**
 * 责任链模式示例（Java 版）
 *
 * 步骤提示：
 * 1. 先准备要处理的数据结构 UserRequest。
 * 2. 写一个抽象 Handler，内置下一个节点和通用的 handle 逻辑。
 * 3. 衍生多个具体 Handler，负责各自的单一职责。
 * 4. 在 main 中按顺序把它们串起来，然后从第一个节点发起 handle。
 */
public class ChainExample {

    public static void main(String[] args) {
        // 第一步：组装一份要处理的请求
        UserRequest request = new UserRequest("Codex", "123456", true);

        // 第二步：创建处理节点
        Handler auth = new AuthHandler();
        Handler network = new NetworkHandler();
        Handler business = new BusinessHandler();

        // 第三步：串成链条，注意顺序就是处理顺序
        auth.setNext(network).setNext(business);

        System.out.println("开始执行责任链...");
        auth.handle(request);
    }
}

/**
 * 请求实体，包含所有节点需要的数据。
 */
class UserRequest {
    private final String userName;
    private final String password;
    private final boolean hasNetwork;

    UserRequest(String userName, String password, boolean hasNetwork) {
        this.userName = userName;
        this.password = password;
        this.hasNetwork = hasNetwork;
    }

    String getUserName() {
        return userName;
    }

    String getPassword() {
        return password;
    }

    boolean hasNetwork() {
        return hasNetwork;
    }
}

/**
 * 抽象 Handler：保存下一个节点，并提供 handle 模板方法。
 */
abstract class Handler {
    private Handler nextHandler;

    Handler setNext(Handler next) {
        this.nextHandler = next;
        return next;
    }

    void handle(UserRequest request) {
        boolean processed = process(request);
        if (!processed && nextHandler != null) {
            nextHandler.handle(request);
        }
    }

    protected abstract boolean process(UserRequest request);
}

/**
 * 校验用户名和密码是否为空。
 */
class AuthHandler extends Handler {
    @Override
    protected boolean process(UserRequest request) {
        if (request.getUserName().isEmpty() || request.getPassword().isEmpty()) {
            System.out.println("AuthHandler: 用户名或密码为空，拦截请求");
            return true; // 处理完毕，不再往后传
        }
        System.out.println("AuthHandler: 基础校验通过，交给下一个");
        return false;
    }
}

/**
 * 检查网络状态。
 */
class NetworkHandler extends Handler {
    @Override
    protected boolean process(UserRequest request) {
        if (!request.hasNetwork()) {
            System.out.println("NetworkHandler: 没有网络，拦截请求");
            return true;
        }
        System.out.println("NetworkHandler: 网络正常，交给下一个");
        return false;
    }
}

/**
 * 真正的业务处理节点。
 */
class BusinessHandler extends Handler {
    @Override
    protected boolean process(UserRequest request) {
        System.out.println("BusinessHandler: 登录成功，欢迎 " + request.getUserName());
        return true;
    }
}

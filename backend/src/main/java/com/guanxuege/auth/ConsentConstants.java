package com.guanxuege.auth;

/**
 * 同意类型与文档版本常量
 * <p>儿童个人信息保护相关：注册时需同意《用户协议》《隐私政策》；
 * 绑定/采集孩子信息时需额外完成监护人关系声明 + 《儿童个人信息保护规则》专项同意。</p>
 */
public final class ConsentConstants {

    /** 注册时同意《用户协议》与《隐私政策》 */
    public static final String TYPE_USER_REGISTER = "USER_REGISTER";

    /** 绑定/采集儿童个人信息时同意《儿童个人信息保护规则》 */
    public static final String TYPE_CHILD_INFO = "CHILD_INFO";

    /** 当前《隐私政策》版本 */
    public static final String PRIVACY_VERSION = "1.0";

    /** 当前《儿童个人信息保护规则》版本 */
    public static final String CHILD_RULES_VERSION = "1.0";

    private ConsentConstants() {
    }
}

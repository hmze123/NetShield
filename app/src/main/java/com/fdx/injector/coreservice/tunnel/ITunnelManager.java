package com.fdx.injector.coreservice.tunnel;

import android.content.Context;

/**
 * هذا هو "العقد" أو الواجهة الموحدة التي يجب أن يلتزم بها كل مدير بروتوكول.
 * إنه يضمن أن كل مدير لديه نفس الوظائف الأساسية، مما يسهل على الخدمة الرئيسية
 * التعامل معهم دون الحاجة لمعرفة تفاصيلهم الداخلية.
 */
public interface ITunnelManager {

    /**
     * تبدأ عملية النفق الخاصة بهذا المدير.
     * @param context سياق التطبيق (Context) لاستخدامه في بدء الخدمات.
     */
    void startTunnel(Context context);

    /**
     * توقف عملية النفق الخاصة بهذا المدير.
     * @param context سياق التطبيق (Context) لاستخدامه في إيقاف الخدمات.
     */
    void stopTunnel(Context context);

    /**
     * تتحقق مما إذا كان النفق الخاص بهذا المدير يعمل حالياً.
     * @return true إذا كان النفق يعمل، وإلا false.
     */
    boolean isRunning();
}
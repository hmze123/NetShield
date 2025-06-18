package com.fdx.injector.coreservice.tunnel;

import android.content.Context;
import com.fdx.injector.coreservice.config.Settings; // استيراد كلاس الإعدادات الخاص بك
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.VpnStatus;

/**
 * هذا هو المدير المسؤول عن نفق OpenVPN (النسخة المصححة).
 */
public class OpenVpnTunnelManager implements ITunnelManager {

    @Override
    public void startTunnel(Context context) {
        // الخطوة 1: الحصول على إعدادات التطبيق
        Settings settings = new Settings(context);

        // الخطوة 2: الحصول على المعرف (UUID) الخاص بالبروفايل الذي اختاره المستخدم
        // سنفترض أنك تحفظ هذا المعرف في الإعدادات عند اختيار المستخدم للملف
        String profileUUID = settings.getOvpnProfileUUID();

        if (profileUUID == null || profileUUID.isEmpty()) {
            VpnStatus.logError("No OpenVPN profile UUID saved in settings!");
            return;
        }

        // الخطوة 3: باستخدام المعرف الصحيح، نحصل الآن على كائن البروفايل الكامل
        VpnProfile profile = ProfileManager.get(context, profileUUID);

        if (profile == null) {
            VpnStatus.logError("Could not find OpenVPN profile with UUID: " + profileUUID);
            return;
        }

        // الخطوة 4: نبدأ الاتصال باستخدام البروفايل الصحيح
        VPNLaunchHelper.startOpenVpn(profile, context);
    }

    @Override
    public void stopTunnel(Context context) {
        ProfileManager.setConntectedVpnProfileDisconnected(context);
    }

    @Override
    public boolean isRunning() {
        return VpnStatus.isVPNActive();
    }
}
-dontwarn
-dontnote
-repackageclasses
-ignorewarnings
-keep public class com.trilead.ssh2.** {
  public protected *;
}
-keep class org.conscrypt.** { *; }
-dontnote libcore.io.Libcore
-dontnote org.apache.harmony.xnet.provider.jsse.OpenSSLRSAPrivateKey
-dontnote org.apache.harmony.security.utils.AlgNameMapper
-dontnote sun.security.x509.AlgorithmId
-dontwarn dalvik.system.BlockGuard
-dontwarn dalvik.system.BlockGuard$Policy
-dontwarn dalvik.system.CloseGuard
-dontwarn com.android.org.conscrypt.AbstractConscryptSocket
-dontwarn com.android.org.conscrypt.ConscryptFileDescriptorSocket
-dontwarn com.android.org.conscrypt.OpenSSLSocketImpl
-dontwarn org.apache.harmony.xnet.provider.jsse.OpenSSLSocketImpl

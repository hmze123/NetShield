package com.fdx.injector.coreservice.config;

public interface SettingsConstants
{
    String[] BYPASS_VPN_PACKAGES = new String[] {
		"com.fdx.injector"
    };
	// Geral
	public static final String
	AUTO_CLEAR_LOGS_KEY = "autoClearLogs",
	HIDE_LOG_KEY = "hideLog",
	MODO_DEBUG_KEY = "modeDebug",
	MODO_NOTURNO_KEY = "modeNight",
	BLOQUEAR_ROOT_KEY = "blockRoot",
	IDIOMA_KEY = "idioma",
	TETHERING_SUBNET = "tetherSubnet",
	DISABLE_DELAY_KEY = "disableDelaySSH",
	MAXIMO_THREADS_KEY = "numberMaxThreadSocks",
	SSH_COMPRESSION = "data_compression",
    WAKELOCK_KEY = "wakelock",
	AUTO_PINGER = "auto_ping",
    HOT_SPOT = "Hotspot",
	URL_KEY = "url",
	FILTER_APPS = "filterApps",
	FILTER_BYPASS_MODE = "filterBypassMode",
	FILTER_APPS_LIST = "filterAppsList",
	NETWORK_SPEED = "speed_meter",
	PROXY_IP_KEY = "proxyRemoto",
	PROXY_PORTA_KEY = "proxyRemotoPorta",
    PROXY_IP_PORT = "proxyport",
	CUSTOM_PAYLOAD_KEY = "proxyPayload",
	PROXY_USAR_DEFAULT_PAYLOAD = "usarDefaultPayload",
	PROXY_USAR_AUTENTICACAO_KEY = "usarProxyAutenticacao"
	;

	public static final String
	CONFIG_PROTEGER_KEY = "protegerConfig",
	CONFIG_MENSAGEM_KEY = "mensagemConfig",
	CONFIG_VALIDADE_KEY = "validadeConfig",
	CONFIG_MENSAGEM_EXPORTAR_KEY = "mensagemConfigExport",
    NAME_CONFIG = "NameConfig",
    S_SAVE = "Save",
    I_PASSWORD = "Ipassword",
    CP_NOTE = "cbNote",
    CP_HWID = "cbHwid",
    ED_POWERED = "edPowered",
    ED_NOTE = "edNote",
    ED_BLOCK_AP = "edblockapp",
    CP_BLOCK_APP = "cbBlockApp",
    ED_HWID = "edhwid",
    M_DATAT_ONLY = "mDataOnly",
    CP_PAYLOAD = "cbPayload",
    CP_SNI = "cpSni",
    CP_SSH = "cpssh",
    CP_DNSTT = "cpDnstt",
    CP_UDP = "cpudp",
    CP_V2RAY = "cpv2ray",
	CONFIG_INPUT_PASSWORD_KEY = "inputPassword"
	;

	// Vpn
	public static final String
    DNSTYPE_KEY = "DNSType",
	DNSFORWARD_KEY = "dnsForward",
	DNSRESOLVER_KEY1 = "dnsResolver1",
	DNSRESOLVER_KEY2 = "dnsResolver2",
	UDPFORWARD_KEY = "udpForward",
	UDPRESOLVER_KEY = "udpResolver";

	//DNS TYPE
	public static final String
	DNS_DEFAULT_KEY = "DNS (Default DNS)",
	DNS_GOOGLE_KEY = "DNS (Google DNS)",
    DNS_DOMAIN_KEY = "DNS (Domain)",
	DNS_CUSTOM_KEY = "DNS (Custom DNS)";

	// SSH
	public static final String
	SERVIDOR_KEY = "sshServer",
	SERVIDOR_PORTA_KEY = "sshPort",
	USUARIO_KEY = "sshUser",
	SENHA_KEY = "sshPass",
	CHAVE_KEY = "chaveKey",
    NAMESERVER_KEY = "serverNameKey",
    DNS_KEY = "dnsKey",
	KEYPATH_KEY = "keyPath",
	PORTA_LOCAL_KEY = "sshPortaLocal",
	//PINGER_KEY = "pingerSSH";
	PINGER = "ping_server";
    
    //udp / v2ray
     public static final String
			UDP_SERVER = "udpserver",
			UDP_AUTH = "udpauth",
			UDP_OBFS = "udpobfs",
			UDP_DOWN = "udpdown",
			UDP_UP = "udpup",
			RECONNECT_UDP = "reconectarudp",
			UDP_BUFFER = "udpbuffer",
            V2RAY_JSON = "v2rayjson";
    

    //ovpn
    public static final String
	AUTO_REPLACE = "AutoReplace",
    
    DETAI_LOG = "DetailLog",
    SHOW_STATS = "ShowStats",
    LOG_LEVE = "LogLevelSlider",

	SSH_HARED = "SshHeared",
    OVPN_HARED = "OvpnHared",
	UDP_HARED = "UDPHared",
	V2RAY_HARED = "V2RAYHared",
	DNSTT_HARED = "DNSTTHared",
    SSL_MODE = "SSLMode",
    
    
    CHZ_21 = "Chz21",
    CHZ_26 = "Chz26",
    CHZ_8 = "chz8",
    CHZ_9 = "chz9",
    
    OVPN_HOST = "ovpnHost",
    OVPN_PORT = "ovpnPort",
    OVPN_CONFIG = "OvpnConfig",
	USUARIO_OVPN = "OvpnUser";

	// Constantes Psiphon
	String PSIPHON_REGION_KEY = "psiphon_region_key";
	String PSIPHON_PROTOCOL_KEY = "psiphon_protocol_key";
	String PSIPHON_BIND_ALL_KEY = "psiphon_bind_all_key";
	String PSIPHON_SNI_KEY = "psiphon_sni_key";
	public static final String
	PAYLOAD_DEFAULT = "CONNECT [host_port] [protocol][crlf][crlf]",
	CUSTOM_SNI = "SNI (eg; example.com)";
	// Trojan Settings Constants

	String TROJAN_REMARKS_KEY = "trojan_remarks";
	String TROJAN_ADDRESS_KEY = "trojan_address";
	String TROJAN_PORT_KEY = "trojan_port";
	String TROJAN_PASSWORD_KEY = "trojan_password";
	String TROJAN_SNI_KEY = "trojan_sni";
	String PSIPHON_AUTHORIZATIONS_KEY = "psiphon_authorizations_key"; // <<<==== جديد
	String PSIPHON_SERVER_ENTRY_KEY = "psiphon_server_entry_key"; // <<<==== جديد
    public static final String
    CB_TUNNELTYPE = "cbTunnelType",
    CB_SNI = "cbSni",
    CB_DNSTT = "cbDnstt",
    CB_V2RAY = "cbV2Ray",
    CB_UDP = "cbUDP",
    CB_PAYLOLOAD = "cbPayload",

	CB_PAYLOLOADOVPN = "cbPayloadOvpn",
			CB_SNIOVPN = "cbSniOvpn",
    CB_PROXY = "cbproxy";
	// Tunnel Type
	public static final String
	TUNNELTYPE_KEY = "tunnelType",
	TUNNEL_TYPE_SSH_DIRECT = "sshDirect",
	TUNNEL_TYPE_SSH_PROXY = "sshProxy",
	TUNNEL_TYPE_PAY_SSL = "sslPayload",
	TUNNEL_TYPE_SSH_SSLTUNNEL = "sshSslTunnel";
	;

	public static final int
	bTUNNEL_TYPE_SSH = 0,
	bTUNNEL_TYPE_SSH_DIRECT = 1,
    bTUNNEL_TYPE_SSH_PROXY = 2,
    bTUNNEL_TYPE_SSH_SSL = 3,
    bTUNNEL_TYPE_PAY_SSL = 4,
    bTUNNEL_TYPE_SSL_RP = 5,
    bTUNNEL_TYPE_SLOWDNS = 6,
    bTUNNEL_TYPE_UDP = 7,
    bTUNNEL_TYPE_V2RAY = 8,

	bTUNNEL_TYPE_OPENVPN = 9,
    bTUNNEL_OVPN_SSL = 10,
    bTUNNEL_OVPN_PROXY = 11,
	// أضف هذا السطر الجديد في نهاية قائمة الأنواع
 bTUNNEL_TYPE_WIREGUARD = 12,
bTUNNEL_TYPE_PSIPHON = 13,
			bTUNNEL_TYPE_TROJAN = 14;
    
}

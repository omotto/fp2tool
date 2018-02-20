package com.example.fp2tool;

/**
 * Created by Worldsensing on 04/01/2018.
 */

public class RFIDReader {

    public static class MemoryAddress {
        public int bank;
        public int offset;
        public int size;
    }

    // RFID Address Bank
    public static final int   BANK_USER     = 0x03;
    public static final int   BANK_TID      = 0x02;
    public static final int   BANK_EPC      = 0x01;
    public static final int   BANK_RESERVED = 0x00;


    public static final byte[] NULL_1 = { 0x0d, 0x0a };
    public static final byte[] NULL_2 = { 0x0d };
    public static final byte[] OPEN_INTERFACE_1 = new byte[] { 0x0d, 0x0a, 0x0d, 0x0a, 0x0d, 0x0a, 0x0d, 0x0a };
    public static final byte[] OPEN_INTERFACE_2 = new byte[] { 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d, 0x0d };

    public static final byte[] BYE = { 'b', 'y', 'e', 0x0d, 0x0a };

    public static final int SKIP_PARAM = 0xffffffff;

    public static int N_TYPE = 1;

    // ---
    public static final String CMD_INVENT = "I";
    public static final String CMD_STOP = "s";
    public static final String CMD_GET_VERSION = "ver";
    public static final String CMD_SET_DEF_PARAM = "Default";
    public static final String CMD_INVENT_PARAM = "Iparam";
    public static final String CMD_GET_PARAM = "g";
    public static final String CMD_SEL_MASK = "M";
    public static final String CMD_SET_TX_POWER = "Txp";
    public static final String CMD_GET_MAX_POWER = "Maxp";
    public static final String CMD_SET_TX_CYCLE = "Txc";
    public static final String CMD_CHANGE_CH_STATE = "Chs";
    public static final String CMD_SET_COUNTRY = "Cc";
    public static final String CMD_GET_COUNTRY_CAP = "ccap";
    public static final String CMD_READ_TAG_MEM = "R";
    public static final String CMD_WRITE_TAG_MEM = "W";
    public static final String CMD_KILL_TAG = "Kill";
    public static final String CMD_LOCK_TAG_MEM = "Lock";
    public static final String CMD_SET_LOCK_TAG_MEM = "lockperm";
    public static final String CMD_PAUSE_TX = "Pause";
    public static final String CMD_HEART_BEAT = "Online";
    public static final String CMD_STATUS_REPORT = "alert";
    public static final String CMD_INVENT_REPORT_FORMAT = "Ireport";
    public static final String CMD_SYSTEM_TIME = "Time";
    public static final String CMD_DISLINK = "bye";

    public static final String CMD_UPLOAD_TAG_DATA = "Br.upl";
    public static final String CMD_CLEAR_TAG_DATA = "Br.clrlist";
    public static final String CMD_ALERT_READER_STATUS = "Br.alert";
    public static final String CMD_GET_STATUS_WORD = "Br.sta";
    public static final String CMD_SET_BUZZER_VOL = "Br.vol";
    public static final String CMD_BEEP = "Br.beep";
    public static final String CMD_SET_AUTO_POWER_OFF_DELAY = "Br.autooff";
    public static final String CMD_GET_BATT_LEVEL = "Br.batt";
    public static final String CMD_REPORT_BATT_STATE = "Br.reportbatt";
    public static final String CMD_TURN_READER_OFF = "Br.off";

    public static final String tagErrorCodeToString(int code) {
        switch (code) {
            case 0x00:  return "general error";
            case 0x03:  return "specified memory location does not exist or the PC value is not supported by the tag";
            case 0x04:  return "specified memory location is locked and/or permalocked and is not writeable";
            case 0x0B:  return "tag has insufficient power to perform the memory write";
            case 0x0F:  return "tag does not support error-specific codes";
        }
        return "Unknown error";
    }

    public static final String moduleErrorCodeToString(int code) {
        switch (code) {
            case 0x01:  return "Read after write verify failed.";
            case 0x02:  return "Problem transmitting tag command.";
            case 0x03:  return "CRC error on tag response to a write.";
            case 0x04:  return "CRC error on the read packet when verifying the write.";
            case 0x05:  return "Maximum retry's on the write exceeded.";
            case 0x06:  return "Failed waiting for read data from tag, possible timeout.";
            case 0x07:  return "Failure requesting a new tag handle.";
            case 0x0A:  return "Error waiting for tag response, possible timeout.";
            case 0x0B:  return "CRC error on tag response to a kill.";
            case 0x0C:  return "Problem transmitting 2nd half of tag kill.";
            case 0x0D:  return "Tag responded with an invalid handle on first kill command.";
            case 0x0F:  return "Bad Access Password.";
        }
        return "Internal Use";
    }

    public int getTypeSize() {
        if (N_TYPE == 1) return 2;
        return 1;
    }

    public byte[] getType()  {
        if (N_TYPE == 1) return NULL_1;
        return NULL_2;
    }

    public static final String getDelimeter() {
        if (N_TYPE == 1) return "\r\n";
        return "\r";
    }

    public byte[] makeMessage (String cmd, int[] param) {
        StringBuilder protocol = new StringBuilder();
        protocol.append(cmd);
        if (param != null && param.length > 0) {
            for (int i = 0; i < param.length; ++i) {
                protocol.append(',');
                if (param[i] != SKIP_PARAM) protocol.append(param[i]);
            }
        }
        return string2bytes(protocol.toString());
    }

    public byte[] makeMessage (String cmd) {
        StringBuilder protocol = new StringBuilder();
        protocol.append(cmd);
        return string2bytes(protocol.toString());
    }

    public byte[] makeMessage (String cmd, String[] options) {
        StringBuilder protocol = new StringBuilder();
        protocol.append(cmd);
        if (options != null && options.length > 0) {
            for (int i = 0; i < options.length; ++i) {
                protocol.append( "," );
                if (options[ i ] != null) protocol.append(options[i]);
            }
        }
        return string2bytes(protocol.toString());
    }

    public byte[] makeMessage (String cmd, int[] param, String[] options, int[] param2) {
        StringBuilder protocol = new StringBuilder();
        protocol.append(cmd);
        if (param != null && param.length > 0) {
            for (int i = 0; i < param.length; ++i) {
                protocol.append(',');
                if (param[i] != SKIP_PARAM) protocol.append(param[i]);
            }
        }
        if (options != null) {
            for (int i = 0; i < options.length; ++i) {
                protocol.append( "," );
                if (options[i] != null) protocol.append(options[i]);
            }
        }
        if (param2 != null && param2.length > 0) {
            for (int i = 0; i < param2.length; ++i) {
                protocol.append(',');
                if (param2[i] != SKIP_PARAM) protocol.append(param2[i]);
            }
        }
        return string2bytes(protocol.toString());
    }

    public byte[] makeMessage (String cmd, int[] param, String option, int[] param2) {
        StringBuilder protocol = new StringBuilder();
        protocol.append(cmd);
        if (param != null && param.length > 0) {
            for (int i = 0; i < param.length; ++i) {
                protocol.append(',');
                if (param[i] != SKIP_PARAM) protocol.append(param[i]);
            }
        }
        protocol.append( "," );
        if (option != null) protocol.append(option);
        if (param2 != null && param2.length > 0) {
            for (int i = 0; i < param2.length; ++i) {
                protocol.append(',');
                if (param2[i] != SKIP_PARAM) protocol.append(param2[i]);
            }
        }
        return string2bytes(protocol.toString());
    }

    public byte[] makeMessage (String cmd, String option, int[] param2) {
        StringBuilder protocol = new StringBuilder();
        protocol.append(cmd);
        protocol.append( "," );
        if (option != null) protocol.append(option);
        if( param2 != null && param2.length > 0 ) {
            for (int i = 0; i < param2.length; ++i) {
                protocol.append(',');
                if (param2[i] != SKIP_PARAM) protocol.append(param2[i]);
            }
        }
        return string2bytes(protocol.toString());
    }

    public byte[] string2bytes(String str) {
        char[] charProtocol = str.toCharArray();
        byte[] byteProtocol = new byte[charProtocol.length + getTypeSize()];
        int index = 0;
        // ---
        for (int i = 0; i < charProtocol.length; ++i, ++index)
            byteProtocol[index] = (byte) (charProtocol[i] & 0xff);
        for (int i = 0; i < getTypeSize(); ++i, ++index)
            byteProtocol[index] = getType()[i];
        return byteProtocol;
    }

    // --- Commands

    public byte[] sendCmdCloseInterface1() {
        return BYE;
    }

    public byte[] sendCmdOpenInterface1() {
        return OPEN_INTERFACE_1;
    }

    public byte[] sendCmdOpenInterface2() {
        return OPEN_INTERFACE_2;
    }

    public void sendCmdInventory() {
        sendCmdInventory(mSingleTag ? 1 : 0, mUseMask ? (mQuerySelected ? 3 : 2) : 0, mTimeout);
    }

    public byte[] sendCmdInventory(int f_s, int f_m, int to) {
        return makeMessage(CMD_INVENT, new int[]{f_s, f_m, to});
    }

    public byte[] sendCmdStop() {
        return makeMessage(CMD_STOP, (int[]) null);
    }

    public byte[] sendHeartBeat(int value) {
        return makeMessage(CMD_HEART_BEAT, new int[]{value});
    }

    public byte[] sendCmdSelectMask(int n, int bits, int mem, int b_offset, String pattern, int target, int action) {
        return makeMessage(CMD_SEL_MASK, new int[]{n, bits, mem, b_offset}, pattern, new int[]{target, action});
    }

    // --- Access
    public byte[] sendSetSession(int session) {
        return makeMessage(CMD_INVENT_PARAM, new int[]{session, SKIP_PARAM, SKIP_PARAM});
    }

    public byte[] sendSetQValue(int q) {
        return makeMessage(CMD_INVENT_PARAM, new int[]{SKIP_PARAM, q, SKIP_PARAM});
    }

    public byte[] sendSetInventoryTarget(int m_ab) {
        return makeMessage(CMD_INVENT_PARAM, new int[]{SKIP_PARAM, SKIP_PARAM, m_ab});
    }

    public byte[] sendInventParam(int session, int q, int m_ab) {
        return makeMessage(CMD_INVENT_PARAM, new int[]{session, q, m_ab});
    }

    public byte[] sendSetSelectAction(int bits, int mem, int b_offset, String pattern, int action) {
        return makeMessage(CMD_SEL_MASK, new int[]{0, bits, mem, b_offset}, pattern, new int[]{SKIP_PARAM, action});
    }

    protected boolean mSingleTag;
    protected boolean mUseMask;
    protected int     mTimeout;
    protected boolean mQuerySelected;

    public void setOpMode(boolean singleTag, boolean useMask, int timeout, boolean querySelected) {
        mSingleTag      = singleTag;
        mUseMask        = useMask;
        mTimeout        = timeout;
        mQuerySelected  = querySelected;
    }

    public byte[] sendReadTag(int w_count, int mem, int w_offset, String ACS_PWD) {
        return makeMessage(CMD_READ_TAG_MEM, new int[]{w_count, mem, w_offset}, ACS_PWD, new int[]{mSingleTag ? 1 : 0, mUseMask ? (mQuerySelected ? 3 : 2) : 0, mTimeout});
    }

    public byte[] sendWriteTag(int w_count, int mem, int w_offset, String ACS_PWD, String wordPattern) {
        return makeMessage(CMD_WRITE_TAG_MEM, new int[]{w_count, mem, w_offset}, new String[]{wordPattern, ACS_PWD}, new int[]{mSingleTag ? 1 : 0, mUseMask ? (mQuerySelected ? 3 : 2) : 0, mTimeout});
    }

    private int convertLockIndex(boolean enable, boolean index) {
        return enable ? (index ? 1 : 0) : -1;
    }

    public static class LockPattern {
        public boolean enableUser;
        public boolean enableTid;
        public boolean enableUii;
        public boolean enableAcsPwd;
        public boolean enableKillPwd;

        public boolean indexUser;
        public boolean indexTid;
        public boolean indexUii;
        public boolean indexAcsPwd;
        public boolean indexKillPwd;

        public boolean lockPerma;
    }

    public byte[] sendLockTag(LockPattern lockPattern, String ACS_PWD) {
        final int user = convertLockIndex(lockPattern.enableUser, lockPattern.indexUser);
        // ( ( lockPattern.indexUser == false
        // ) ? ( R900Protocol.SKIP_PARAM ) :
        // ( lockPattern.enableUser ? 1 : 0
        // ) );
        final int tid = convertLockIndex(lockPattern.enableTid, lockPattern.indexTid);
        // ( ( lockPattern.indexTid == false )
        // ? ( R900Protocol.SKIP_PARAM ) : (
        // lockPattern.enableTid ? 1 : 0 )
        // );
        final int epc = convertLockIndex(lockPattern.enableUii, lockPattern.indexUii);
        // ( ( lockPattern.indexUii == false )
        // ? ( R900Protocol.SKIP_PARAM ) : (
        // lockPattern.enableUii ? 1 : 0 )
        // );
        final int acs_pwd = convertLockIndex(lockPattern.enableAcsPwd, lockPattern.indexAcsPwd);
        // ( ( lockPattern.indexAcsPwd ==
        // false ) ? (
        // R900Protocol.SKIP_PARAM ) : (
        // lockPattern.enableAcsPwd ? 1
        // : 0 ) );
        final int kill_pwd = convertLockIndex(lockPattern.enableKillPwd, lockPattern.indexKillPwd);
        // ( ( lockPattern.indexKillPwd ==
        // false ) ? (
        // R900Protocol.SKIP_PARAM ) : (
        // lockPattern.enableKillPwd ? 1
        // : 0 ) );
        return makeMessage(CMD_LOCK_TAG_MEM, new int[]{user, tid, epc, acs_pwd, kill_pwd}, ACS_PWD, new int[]{mSingleTag ? 1 : 0, mUseMask ? (mQuerySelected ? 3 : 2) : 0, mTimeout});
    }

    public byte[] sendLockTag(int lockMask, int lockEnable, String ACS_PWD) {
        int     bitFlag;
        boolean mask;
        boolean enable;
        // ---
        bitFlag = 0x200;// 0x02;
        mask = (lockMask & bitFlag) == bitFlag;
        enable = (lockEnable & bitFlag) == bitFlag;
        final int user = ((mask == false) ? (SKIP_PARAM) : (enable ? 1 : 0));
        // ---
        bitFlag = 0x80;// 0x08;
        mask = (lockMask & bitFlag) == bitFlag;
        enable = (lockEnable & bitFlag) == bitFlag;
        final int tid = ((mask == false) ? (SKIP_PARAM) : (enable ? 1 : 0));
        // ---
        bitFlag = 0x20;
        mask = (lockMask & bitFlag) == bitFlag;
        enable = (lockEnable & bitFlag) == bitFlag;
        final int epc = ((mask == false) ? (SKIP_PARAM) : (enable ? 1 : 0));
        // ---
        bitFlag = 0x08;// 0x80;
        mask = (lockMask & bitFlag) == bitFlag;
        enable = (lockEnable & bitFlag) == bitFlag;
        final int acs_pwd = ((mask == false) ? (SKIP_PARAM) : (enable ? 1 : 0));
        // ---
        bitFlag = 0x02;// 0x200;
        mask = (lockMask & bitFlag) == bitFlag;
        enable = (lockEnable & bitFlag) == bitFlag;
        final int kill_pwd = ((mask == false) ? (SKIP_PARAM) : (enable ? 1 : 0));
        // ---
        return makeMessage(CMD_LOCK_TAG_MEM, new int[]{user, tid, epc, acs_pwd, kill_pwd}, ACS_PWD, new int[]{mSingleTag ? 1 : 0, mUseMask ? (mQuerySelected ? 3 : 2) : 0, mTimeout});
    }

    public byte[] sendKillTag(String killPwd) {
        return makeMessage(CMD_KILL_TAG, killPwd, new int[]{mSingleTag ? 1 : 0, mUseMask ? (mQuerySelected ? 3 : 2) : 0, mTimeout});
    }

    // ---- other commands
    public byte[] sendGetVersion() {
        return makeMessage(CMD_GET_VERSION);
    }

    public byte[] sendSetDefaultParameter() {
        return makeMessage(CMD_SET_DEF_PARAM);
    }

    public byte[] sendGettingParameter(String cmd, String p) {
        return makeMessage(CMD_GET_PARAM, new String[]{cmd, p});
    }

    public byte[] sendSettingTxPower(int a) {
        return makeMessage(CMD_SET_TX_POWER, new int[]{a});
    }

    public byte[] sendGetMaxPower() {
        return makeMessage(CMD_GET_MAX_POWER);
    }

    public byte[] sendSettingTxCycle(int on, int off) {
        return makeMessage(CMD_SET_TX_CYCLE, new int[]{on, off});
    }

    public byte[] sendChangeChannelState(int n, int f_e) {
        return makeMessage(CMD_CHANGE_CH_STATE, new int[]{n, f_e});
    }

    public byte[] sendSettingCountry(int code) {
        return makeMessage(CMD_CHANGE_CH_STATE, new int[]{code});
    }

    public byte[] sendGettingCountry() {
        return makeMessage(CMD_GET_COUNTRY_CAP);
    }

    public byte[] sendSetLockTagMemStatePerm(int mem_id, int f_l, String ACS_PWD) {
        return makeMessage(CMD_SET_LOCK_TAG_MEM, new int[]{mem_id, f_l}, ACS_PWD, new int[]{ mSingleTag ? 1 : 0, mUseMask ? (mQuerySelected ? 3 : 2) : 0, mTimeout});
    }

    public byte[] sendPauseTx() {
        return makeMessage(CMD_PAUSE_TX);
    }

    public byte[] sendStatusReporting(int f_link) {
        return makeMessage(CMD_STATUS_REPORT, new int[]{f_link});
    }

    public byte[] sendInventoryReportingFormat(int f_time, int f_rssi) {
        return makeMessage(CMD_INVENT_REPORT_FORMAT, new int[]{ f_time, f_rssi});
    }

    public byte[] sendDislink() {
        return makeMessage(CMD_DISLINK);
    }

    // ---- R900 Controls
    public byte[] sendUploadingTagData(int index, int count) {
        return makeMessage(CMD_UPLOAD_TAG_DATA, new int[]{index, count});
    }

    public byte[] sendClearingTagData() {
        return makeMessage(CMD_CLEAR_TAG_DATA);
    }

    public byte[] sendAlertReaderStatus(int f_link, int f_trigger, int f_lowbat, int f_autooff, int f_pwr) {
        return makeMessage(CMD_ALERT_READER_STATUS, new int[]{f_link, f_trigger, f_lowbat, f_autooff, f_pwr});
    }

    public byte[] sendGettingStatusWord() {
        return makeMessage(CMD_GET_STATUS_WORD);
    }

    public byte[] sendSettingBuzzerVolume(int volume, int f_nv) {
        return makeMessage(CMD_SET_BUZZER_VOL, new int[]{volume, f_nv});
    }

    public byte[] sendBeep(int f_on) {
        return makeMessage(CMD_BEEP, new int[]{f_on});
    }

    public byte[] sendSettingAutoPowerOffDelay(int delay, int f_nv) {
        return makeMessage(CMD_SET_AUTO_POWER_OFF_DELAY, new int[]{delay, f_nv});
    }

    public byte[] sendGettingBatteryLevel(int f_ext) {
        return makeMessage(CMD_GET_BATT_LEVEL, new int[]{f_ext});
    }

    public byte[] sendReportingBatteryState(int f_report) {
        return makeMessage(CMD_REPORT_BATT_STATE, new int[]{f_report});
    }

    public byte[] sendTurningReaderOff() {
        return makeMessage(CMD_TURN_READER_OFF);
    }

    // -- Parse RX messages
    private String messageBuffer = "";
    private String message;
    synchronized public String getMessage(String readMessage) {
        String message = "";
        messageBuffer = messageBuffer.concat(readMessage);
        int subStringPos = messageBuffer.indexOf("$>");
        if (subStringPos >= 0) {
            message = messageBuffer.substring(0, subStringPos + 2);
            messageBuffer = messageBuffer.substring(subStringPos+2, messageBuffer.length());
        }
        return message;
    }

    synchronized public void pushMessage(String message) {
        this.message = new String(message);
    }

    synchronized public String getMessageEPC() {
        int subStringPos = messageBuffer.indexOf(",e=");
        if (subStringPos  >= 0)
            return this.message.substring(subStringPos+3, subStringPos + 3 + 32);
        return null;
    }

    synchronized public int checkMessage(String EPC, boolean read) {
        int subStringPos;
        // -- NOT OK
        subStringPos = this.message.indexOf("ok");
        if (subStringPos < 0) return 1;
        // -- NOT END WRITING OPERATION
        subStringPos = this.message.indexOf("end=0,w");
        if ((read == false) && (subStringPos < 0)) return 2;
        // -- NOT END READING OPERATION
        subStringPos = this.message.indexOf("end=0,r");
        if ((read == true) && (subStringPos < 0)) return 3;
        // -- NOT EPC
        subStringPos = this.message.indexOf(",e=");
        if (subStringPos < 0) return 4;
        else
            // -- EPC NOTE EQUALS
            if ((!this.message.substring(subStringPos + 3, subStringPos + 3 + 32).equals(EPC)) && EPC != null) return 5;
        // -- OPERATION ERROR
        subStringPos = this.message.indexOf("err_op");
        if (subStringPos >= 0) return 6;
        // -- TAG ERROR
        subStringPos = this.message.indexOf("err_tag");
        if (subStringPos >= 0) return 7;
        return 0;
    }

    synchronized public String parseMessageDataAs(int commandCode) {
        int subStringPos;
        String retorno = new String();
        subStringPos = this.message.indexOf(",e=");
        // Get NODE ID from EPC value
        retorno = retorno + "Node ID        : " + String.valueOf(ByteArrayToInteger(hexStringToByteArray(this.message.substring(subStringPos + 25, subStringPos + 31)))&0xFFFFFF) + " d\r\n";
        switch (commandCode) {
            case 0x01: // SET_ACTIVE_CMD
            case 0x02: // SET_STANDBY_CMD
            case 0x03: // SET_DEBUG_CMD
            case 0x06: // SET_THRESHOLD_CMD
                // Reply    Data Length   Node ID   Seq. Num.   Message Type    Reply Code  CRC     Paddding
                // 00 01    00 07         C0 50     56 02       00              00 00       33 5E   00
                retorno = retorno + "Replied        : " + this.message.substring(subStringPos - 28, subStringPos - 24) + " h\r\n";
                retorno = retorno + "Data Length    : " + this.message.substring(subStringPos - 24, subStringPos - 20) + " h\r\n";
                retorno = retorno + "Node ID        : " + this.message.substring(subStringPos - 20, subStringPos - 16) + " h\r\n";
                retorno = retorno + "Seq. Num.      : " + this.message.substring(subStringPos - 16, subStringPos - 12) + " h\r\n";
                retorno = retorno + "Message Type   : " + this.message.substring(subStringPos - 12, subStringPos - 10) + " h\r\n";
                retorno = retorno + "Reply Code     : " + this.message.substring(subStringPos - 10, subStringPos - 6 ) + " h\r\n";
                retorno = retorno + "CRC            : " + this.message.substring(subStringPos - 6 , subStringPos - 2 ) + " h\r\n";
                retorno = retorno + "Padding        : " + this.message.substring(subStringPos - 2 , subStringPos - 0 ) + " h\r\n";
                return retorno;
            case 0x04: // GET_FIRMWARE_CMD
                // Protocol(2bits) Mote ID(24bits) Seq.Num.(6bits)  AM_TYPE FW Ver. MSB FW Ver. Protocol    Build Time      CRC
                // C0 50 56 01                                      29      01      03          01          5A 3A 64 9E     5CC5
                retorno = retorno + "Protocol Ver.  : " + String.valueOf(((int)hexStringToByteArray(this.message.substring(subStringPos - 28, subStringPos - 26))[0]&0xC0)>>6) + " d\r\n";
                retorno = retorno + "Unique Mote ID : " + this.message.substring(subStringPos - 28, subStringPos - 20) + " h\r\n";
                retorno = retorno + "Seq. Num.      : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 22, subStringPos - 20))[0]&0x3F) + " d\r\n";
                retorno = retorno + "AM_TYPE        : " + this.message.substring(subStringPos - 20, subStringPos - 18) + " h\r\n";
                retorno = retorno + "FW Version MSB : " + this.message.substring(subStringPos - 18, subStringPos - 16) + " h\r\n";
                retorno = retorno + "FW Version LSB : " + this.message.substring(subStringPos - 16, subStringPos - 14) + " h\r\n";
                retorno = retorno + "Protocol Ver.  : " + this.message.substring(subStringPos - 14, subStringPos - 12) + " h\r\n";
                retorno = retorno + "Build Time     : " + this.message.substring(subStringPos - 12, subStringPos - 4 ) + " h\r\n";
                retorno = retorno + "CRC            : " + this.message.substring(subStringPos - 4 , subStringPos - 0 ) + " h\r\n";
                return retorno;
            case 0x05: // GET_STATUS_CMD
                // Protocol(2bits) Mote ID(24bits) Seq.Num.(6bits)  AM_TYPE     Status  CRC
                // C0 50 56 00                                      2C          01      BA2C
                retorno = retorno + "Protocol Ver.  : " + String.valueOf(((int)hexStringToByteArray(this.message.substring(subStringPos - 16, subStringPos - 14))[0]&0xC0)>>6) + " d\r\n";
                retorno = retorno + "Unique Mote ID : " + this.message.substring(subStringPos - 16, subStringPos - 8) + " h\r\n";
                retorno = retorno + "Seq. Num.      : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 10, subStringPos - 8))[0]&0x3F) + " d\r\n";
                retorno = retorno + "AM_TYPE        : " + this.message.substring(subStringPos - 8 , subStringPos - 6) + " h\r\n";
                retorno = retorno + "Status         : " + this.message.substring(subStringPos - 6 , subStringPos - 4) + " h";
                if (this.message.substring(subStringPos - 6 , subStringPos - 4 ).equals("00"))
                    retorno = retorno + " (StandBy)\r\n";
                else
                    if (this.message.substring(subStringPos - 6 , subStringPos - 4 ).equals("01"))
                        retorno = retorno + " (Active)\r\n";
                    else
                        retorno = retorno + " (Unknown)\r\n";
                retorno = retorno + "CRC            : " + this.message.substring(subStringPos - 4 , subStringPos - 0 ) + " h\r\n";
                return retorno;
            case 0x07: // GET_THRESHOLD_CMD
                // Reply    Data Length     Node ID     Seq. Num.   Message Type
                // 0001     0018            c050        5609        80              0fa0 50 23 23 23 41 03 46 46 46 1e 14 32 04 18 48 00 1e
                retorno = retorno + "Replied        : " + this.message.substring(subStringPos - 56, subStringPos - 52) + " h\r\n";
                retorno = retorno + "Data Length    : " + this.message.substring(subStringPos - 52, subStringPos - 48) + " h\r\n";
                retorno = retorno + "Node ID        : " + this.message.substring(subStringPos - 48, subStringPos - 44) + " h\r\n";
                retorno = retorno + "Seq. Num.      : " + this.message.substring(subStringPos - 44, subStringPos - 40) + " h\r\n";
                retorno = retorno + "Message Type   : " + this.message.substring(subStringPos - 40, subStringPos - 38) + " h (Must be 80h)\r\n";
                retorno = retorno + "Absolute Threshold     : " + String.valueOf(ByteArrayToInteger(hexStringToByteArray(this.message.substring(subStringPos - 38, subStringPos - 34)))&0xFFFF) + " d\r\n";
                retorno = retorno + "IR Threshold           : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 34, subStringPos - 32))[0]&0xff) + " d\r\n";
                retorno = retorno + "Mag Perturbation Th X  : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 32, subStringPos - 30))[0]&0xff) + " d\r\n";
                retorno = retorno + "Mag Perturbation Th Y  : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 30, subStringPos - 28))[0]&0xff) + " d\r\n";
                retorno = retorno + "Mag Perturbation Th Z  : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 28, subStringPos - 26))[0]&0xff) + " d\r\n";
                retorno = retorno + "Mag Update Max Shift   : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 26, subStringPos - 24))[0]&0xff) + " d\r\n";
                retorno = retorno + "Mag Hysteresis         : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 24, subStringPos - 22))[0]&0xff) + " d\r\n";
                retorno = retorno + "Mag Absolute Th X      : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 22, subStringPos - 20))[0]&0xff) + " d\r\n";
                retorno = retorno + "Mag Absolute Th Y      : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 20, subStringPos - 18))[0]&0xff) + " d\r\n";
                retorno = retorno + "Mag Absolute Th Z      : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 18, subStringPos - 16))[0]&0xff) + " d\r\n";
                retorno = retorno + "Stable Time            : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 16, subStringPos - 14))[0]&0xff) + " d\r\n";
                retorno = retorno + "Mag Value Table Life   : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 14, subStringPos - 12))[0]&0xff) + " d\r\n";
                retorno = retorno + "IR Variation Check     : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 12, subStringPos - 10))[0]&0xff) + " d\r\n";
                retorno = retorno + "IR Linearity Correction: " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 10, subStringPos -  8))[0]&0xff) + " d\r\n";
                retorno = retorno + "Optical Free Timeout   : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos -  8, subStringPos -  6))[0]&0xff) + " d\r\n";
                retorno = retorno + "Optical Busy Timeout   : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos -  6, subStringPos -  4))[0]&0xff) + " d\r\n";
                retorno = retorno + "Enable Sat. Mag Update : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos -  4, subStringPos -  2))[0]&0xff) + " d\r\n";
                retorno = retorno + "Enable IR Noise Mag    : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos -  2, subStringPos -  0))[0]&0xff) + " d\r\n";
                return retorno;
            case 0x08: // GET_DEBUGINFO_CMD
                // Reply    Data Length     Node ID     Seq. Num.   Message Type    NumericState    IR1             IR2             IR3             MagX    MagY    MagZ    MagThX  MagThy  MagThz
                // 0001     001e            c050        5603        2d              09              00 00 00 63     00 00 00 63     00 00 0f ee     ff 9b   ff 72   ff ea   05 0a   05 0a   05 0a
                retorno = retorno + "Repplied       : " + this.message.substring(subStringPos - 68, subStringPos - 64) + " h\r\n";
                retorno = retorno + "Data Length    : " + this.message.substring(subStringPos - 64, subStringPos - 60) + " h\r\n";
                retorno = retorno + "Node ID        : " + this.message.substring(subStringPos - 60, subStringPos - 56) + " h\r\n";
                retorno = retorno + "Seq. Num.      : " + this.message.substring(subStringPos - 56, subStringPos - 52) + " h\r\n";
                retorno = retorno + "Message Type   : " + this.message.substring(subStringPos - 52, subStringPos - 50) + " h (Must be 2Dh)\r\n";
                retorno = retorno + "Numerical State        : " + String.valueOf((int)hexStringToByteArray(this.message.substring(subStringPos - 50, subStringPos - 48))[0]) + " d\r\n";
                retorno = retorno + "IR 1                   : " + String.valueOf(ByteArrayToInteger(hexStringToByteArray(this.message.substring(subStringPos - 48, subStringPos - 40)))&0xFFFF) + " d\r\n";
                retorno = retorno + "IR 2                   : " + String.valueOf(ByteArrayToInteger(hexStringToByteArray(this.message.substring(subStringPos - 40, subStringPos - 32)))&0xFFFF) + " d\r\n";
                retorno = retorno + "IR 3                   : " + String.valueOf(ByteArrayToInteger(hexStringToByteArray(this.message.substring(subStringPos - 32, subStringPos - 24)))&0xFFFF) + " d\r\n";
                retorno = retorno + "Magnetic X             : " + String.valueOf(unsignedTosignedWord(ByteArrayToInteger(hexStringToByteArray(this.message.substring(subStringPos - 24, subStringPos - 20)))&0xFFFF)) + " d\r\n";
                retorno = retorno + "Magnetic Y             : " + String.valueOf(unsignedTosignedWord(ByteArrayToInteger(hexStringToByteArray(this.message.substring(subStringPos - 20, subStringPos - 16)))&0xFFFF)) + " d\r\n";
                retorno = retorno + "Magnetic Z             : " + String.valueOf(unsignedTosignedWord(ByteArrayToInteger(hexStringToByteArray(this.message.substring(subStringPos - 16, subStringPos - 12)))&0xFFFF)) + " d\r\n";
                retorno = retorno + "Magnetic  Threshold X  : " + String.valueOf(unsignedTosignedWord(ByteArrayToInteger(hexStringToByteArray(this.message.substring(subStringPos - 12, subStringPos -  8)))&0xFFFF)) + " d\r\n";
                retorno = retorno + "Magnetic  Threshold Y  : " + String.valueOf(unsignedTosignedWord(ByteArrayToInteger(hexStringToByteArray(this.message.substring(subStringPos -  8, subStringPos -  4)))&0xFFFF)) + " d\r\n";
                retorno = retorno + "Magnetic  Threshold Z  : " + String.valueOf(unsignedTosignedWord(ByteArrayToInteger(hexStringToByteArray(this.message.substring(subStringPos -  4, subStringPos -  0)))&0xFFFF)) + " d\r\n";
                return retorno;
        }
        retorno = "Unknown Operation\r\n";
        return retorno;
    }

    // -- Tools

    public static int unsignedTosignedWord(int inputValue) {
        //int signedValue = (inputValue << 1) >> 1;
        int signedValue = inputValue;
        if (signedValue > 32768) signedValue = (signedValue - 32768) * -1;
        return signedValue;
    }

    public static int ByteArrayToInteger(byte [] bytes) {
        int retInt = 0;
        for (byte b: bytes) {
            retInt = retInt | (b&0x00FF);
            retInt = retInt << 8;
        }
        retInt = retInt >> 8;
        return retInt/*&0xffff*/;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        return data;
    }

    public String crc16_ccitt(byte [] bytes) {
        int crc = 0x0000;          // initial value
        int polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12)

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b   >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15    & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }
        return String.format("%04X", crc & 0xffff);
    }
}

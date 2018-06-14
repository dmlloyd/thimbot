/*
 * Copyright 2017 by David M. Lloyd and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flurg.thimbot.event.irc.response;

/**
 */
public final class ResponseCode {
    public static final int WELCOME = 1;
    public static final int YOUR_HOST = 2;
    public static final int CREATED = 3;
    public static final int SERVER_INFO = 4;
    public static final int SERVER_SUPPORTS = 5;
    public static final int MAP = 6;
    public static final int MAP_END = 7;
    public static final int SERVER_NOTICE_MASK = 8;
    public static final int BOUNCE = 10;
    public static final int YOUR_ID = 42;
    public static final int SAVE_NICK = 43;
    public static final int USER_MODE = 221;
    public static final int SERVLIST = 234;
    public static final int SERVLIST_END = 235;

    public static final int NONE = 300;

    public static final int AWAY = 301;
    public static final int UNAWAY = 305;
    public static final int NOW_AWAY = 306;

    public static final int WHOIS_USER = 311;
    public static final int WHOIS_SERVER = 312;
    public static final int WHOIS_OPERATOR = 313;
    public static final int WHOWAS_USER = 314;
    public static final int WHO_END = 315;
    public static final int WHOIS_CHANOP = 316;
    public static final int WHOIS_IDLE = 317;
    public static final int WHOIS_END = 318;
    public static final int WHOIS_CHANNELS = 319;

    public static final int CHANNEL_LIST_START = 321;
    public static final int CHANNEL_LIST = 322;
    public static final int LIST_END = 323;
    public static final int CHANNEL_MODE = 324;
    public static final int CHANNEL_PASSWORD = 325;
    public static final int NO_CHANNEL_PASSWORD = 326;
    public static final int CHANNEL_PASSWORD_UNKNOWN = 327;
    public static final int CHANNEL_URL = 328;
    public static final int CHANNEL_CREATE_TIME = 329;

    public static final int CHANNEL_NO_TOPIC = 331;
    public static final int CHANNEL_TOPIC = 332;
    public static final int CHANNEL_TOPIC_WHO_TIME = 333;

    public static final int INVITING = 341;
    public static final int SUMMONING = 342;
    public static final int INVITED = 345;

    public static final int INVITE_LIST = 346;
    public static final int INVITE_LIST_END = 347;
    public static final int EXCEPT_LIST = 348;
    public static final int EXCEPT_LIST_END = 349;

    public static final int VERSION = 351;

    public static final int WHO_REPLY = 352;
    public static final int NAMES_REPLY = 353;

    public static final int LINKS = 364;
    public static final int LINKS_END = 365;

    public static final int NAMES_END = 366;

    public static final int BAN_LIST = 367;
    public static final int BAN_LIST_END = 368;

    public static final int WHOWAS_END = 369;

    public static final int INFO = 371;
    public static final int MOTD = 372;
    public static final int INFO_END = 373;
    public static final int MOTD_START = 375;
    public static final int MOTD_END = 376;
    public static final int TIME = 391;

    public static final int USERS_START = 392;
    public static final int USERS = 393;
    public static final int USERS_END = 394;
    public static final int NO_USERS = 395;
    public static final int HOST_HIDDEN = 396;

    public static final int ERR_UNKNOWN = 400;

    public static final int ERR_NO_SUCH_NICK = 401;
    public static final int ERR_NO_SUCH_SERVER = 402;
    public static final int ERR_NO_SUCH_CHANNEL = 403;
    public static final int ERR_CANNOT_SEND_TO_CHANNEL = 404;
    public static final int ERR_TOO_MANY_CHANNELS = 405;
    public static final int ERR_WHOWAS_NO_SUCH_NICK = 406;
    public static final int ERR_TOO_MANY_TARGETS = 407;
    public static final int ERR_NO_SUCH_SERVICE = 408;
    public static final int ERR_NO_ORIGIN = 409;
    public static final int ERR_BAD_CAP_COMMAND = 410;
    public static final int ERR_NO_RECIPIENT = 411;
    public static final int ERR_NO_TEXT_TO_SEND = 412;
    public static final int ERR_NOT_TOP_LEVEL = 413;
    public static final int ERR_WILDCARD_TOP_LEVEL = 414;
    public static final int ERR_BAD_MASK = 415;
    public static final int ERR_TOO_MANY_MATCHES = 416;

    public static final int ERR_UNKNOWN_COMMAND = 421;
    public static final int ERR_NO_MOTD = 422;
    public static final int ERR_NO_ADMIN_INFO = 423;
    public static final int ERR_FILE_ERROR = 424;
    public static final int ERR_EVENT_NICK_CHANGE = 430;
    public static final int ERR_NO_NICK_GIVEN = 431;
    public static final int ERR_INVALID_NICK = 432;
    public static final int ERR_NICK_IN_USE = 433;
    public static final int ERR_NICK_COLLISION = 436;
    public static final int ERR_UNAVAILABLE_RESOURCE = 437;
    public static final int ERR_NICK_TOO_FAST = 438;
    public static final int ERR_TARGET_TOO_FAST = 439;

    public static final int ERR_USER_NOT_IN_CHANNEL = 441;
    public static final int ERR_NOT_IN_CHANNEL = 442;
    public static final int ERR_USER_IN_CHANNEL = 443;
    public static final int ERR_NO_LOGIN = 444;
    public static final int ERR_SUMMON_DISABLED = 445;
    public static final int ERR_USERS_DISABLED = 446;
    public static final int ERR_NOT_IMPLEMENTED = 449;

    public static final int ERR_NOT_REGISTERED = 451;
    public static final int ERR_ID_COLLISION = 452;
    public static final int ERR_NEED_MORE_PARAMS = 461;
    public static final int ERR_ALREADY_REGISTERED = 462;
    public static final int ERR_NO_PERMISSION_FOR_HOST = 463;
    public static final int ERR_PASSWORD_MISMATCH = 464;
    public static final int ERR_BANNED = 465;
    public static final int ERR_WILL_BE_BANNED = 466;

    public static final int ERR_KEY_SET = 467;
    public static final int ERR_CHANNEL_REDIRECT = 470; // >> :weber.freenode.net 470 dmlloyd #linux ##linux :Forwarding to another channel
    public static final int ERR_CHANNEL_FULL = 471;
    public static final int ERR_UNKNOWN_CHANNEL_MODE = 472;
    public static final int ERR_INVITE_ONLY_CHANNEL = 473;
    public static final int ERR_BANNED_FROM_CHANNEL = 474;
    public static final int ERR_BAD_CHANNEL_KEY = 475;
    public static final int ERR_BAD_CHANNEL_MASK = 476;
    public static final int ERR_NO_CHANNEL_MODES = 477;
    public static final int ERR_BAN_LIST_FULL = 478;

    public static final int ERR_NO_PRIVILEGES = 481;
    public static final int ERR_NOT_CHANOP = 482;
    public static final int ERR_CANT_KILL_SERVER = 483;
    public static final int ERR_RESTRICTED = 484;
    public static final int ERR_NOT_CHAN_CREATOR = 485;

    public static final int ERR_NO_OPER_HOST = 491;
    public static final int ERR_NO_SERVICE_HOST = 492;

    public static final int ERR_UNKNOWN_USER_MODE = 501;
    public static final int ERR_USERS_DONT_MATCH = 502;

    public static final int LOGGED_IN = 900;
    public static final int LOGGED_OUT = 901;
    public static final int ERR_NICK_LOCKED = 902;
    public static final int SASL_SUCCESS = 903;
    public static final int ERR_SASL_FAIL = 904;
    public static final int ERR_SASL_TOO_LONG = 905;
    public static final int ERR_SASL_ABORTED = 906;
    public static final int ERR_SASL_ALREADY_AUTHED = 907;
    public static final int SASL_MECHS = 908;

    private ResponseCode() {}
}

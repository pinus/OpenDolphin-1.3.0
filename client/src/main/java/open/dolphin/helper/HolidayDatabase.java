package open.dolphin.helper;

/**
 * 休日データベース.
 * http://www.a21-hp.com/_shared/css/holidays.csv から引用.
 * sed -e 's/\(^.*\), *\(.*\)$/{ "\1", "\2" },/g' にて変換.
 *
 * @author pns
 */
public class HolidayDatabase {

    public static final String[][] HOLIDAY_DATA = {
            // 当院独自のデータ
            {"20190202", "臨時休業"},
            {"20190427", "臨時休業"},
            {"20190607", "学会休診"},
            {"20190608", "学会休診"},
            {"20190815", "お盆休み"},
            {"20190816", "お盆休み"},
            {"20190817", "お盆休み"},
            {"20191003", "臨時休業"},
            {"20191004", "臨時休業"},
            {"20191005", "臨時休業"},
            {"20191007", "臨時休業"},
            {"20191008", "臨時休業"},
            {"20191230", "正月休み"},
            {"20191231", "正月休み"},
            {"20200102", "正月休み"},
            {"20200103", "正月休み"},
            {"20200104", "正月休み"},

            {"20180501", "臨時休業"},
            {"20180531", "学会休診"},
            {"20180601", "学会休診"},
            {"20180602", "学会休診"},
            {"20180813", "お盆休み"},
            {"20180814", "お盆休み"},
            {"20181009", "臨時休業"},
            {"20181011", "臨時休業"},
            {"20181012", "臨時休業"},
            {"20181013", "臨時休業"},
            {"20181229", "正月休み"},
            {"20181230", "正月休み"},
            {"20181231", "正月休み"},
            {"20190102", "正月休み"},
            {"20190103", "正月休み"},

            {"20170501", "臨時休業"},
            {"20170502", "臨時休業"},
            {"20170506", "臨時休業"},
            {"20170601", "学会休診"},
            {"20170602", "学会休診"},
            {"20170603", "学会休診"},
            {"20170812", "お盆休み"},
            {"20170814", "お盆休み"},
            {"20170815", "お盆休み"},
            {"20171010", "臨時休業"},
            {"20171012", "臨時休業"},
            {"20171013", "臨時休業"},
            {"20171014", "臨時休業"},
            {"20171107", "臨時休業"},
            {"20171109", "臨時休業"},
            {"20171229", "正月休み"},
            {"20171230", "正月休み"},
            {"20180102", "正月休み"},

            // http://www.a21-hp.com/_shared/css/holidays.csv から引用.
            // sed -e 's/\(^.*\), *\(.*\)$/{ "\1", "\2" },/g' にて変換.
            {"20100101", "元日"},
            {"20100111", "成人の日"},
            {"20100211", "建国記念の日"},
            {"20100321", "春分の日"},
            {"20100322", "振替休日"},
            {"20100429", "昭和の日"},
            {"20100503", "憲法記念日"},
            {"20100504", "みどりの日"},
            {"20100505", "こどもの日"},
            {"20100719", "海の日"},
            {"20100920", "敬老の日"},
            {"20100923", "秋分の日"},
            {"20101011", "体育の日"},
            {"20101103", "文化の日"},
            {"20101123", "勤労感謝の日"},
            {"20101223", "天皇誕生日"},
            {"20101224", "振替休日"},
            {"20110101", "元日"},
            {"20110110", "成人の日"},
            {"20110211", "建国記念の日"},
            {"20110321", "春分の日"},
            {"20110429", "昭和の日"},
            {"20110503", "憲法記念日"},
            {"20110504", "みどりの日"},
            {"20110505", "こどもの日"},
            {"20110718", "海の日"},
            {"20110919", "敬老の日"},
            {"20110923", "秋分の日"},
            {"20111010", "体育の日"},
            {"20111103", "文化の日"},
            {"20111123", "勤労感謝の日"},
            {"20111223", "天皇誕生日"},
            {"20120101", "元日"},
            {"20120102", "振替休日"},
            {"20120109", "成人の日"},
            {"20120211", "建国記念の日"},
            {"20120320", "春分の日"},
            {"20120429", "昭和の日"},
            {"20120430", "振替休日"},
            {"20120503", "憲法記念日"},
            {"20120504", "みどりの日"},
            {"20120505", "こどもの日"},
            {"20120716", "海の日"},
            {"20120917", "敬老の日"},
            {"20120922", "秋分の日"},
            {"20121008", "体育の日"},
            {"20121103", "文化の日"},
            {"20121123", "勤労感謝の日"},
            {"20121223", "天皇誕生日"},
            {"20121224", "振替休日"},
            {"20130101", "元日"},
            {"20130114", "成人の日"},
            {"20130211", "建国記念の日"},
            {"20130320", "春分の日"},
            {"20130429", "昭和の日"},
            {"20130503", "憲法記念日"},
            {"20130504", "みどりの日"},
            {"20130505", "こどもの日"},
            {"20130506", "振替休日"},
            {"20130715", "海の日"},
            {"20130916", "敬老の日"},
            {"20130923", "秋分の日"},
            {"20131014", "体育の日"},
            {"20131103", "文化の日"},
            {"20131104", "振替休日"},
            {"20131123", "勤労感謝の日"},
            {"20131223", "天皇誕生日"},
            {"20140101", "元日"},
            {"20140113", "成人の日"},
            {"20140211", "建国記念の日"},
            {"20140321", "春分の日"},
            {"20140429", "昭和の日"},
            {"20140503", "憲法記念日"},
            {"20140504", "みどりの日"},
            {"20140505", "こどもの日"},
            {"20140506", "振替休日"},
            {"20140721", "海の日"},
            {"20140915", "敬老の日"},
            {"20140923", "秋分の日"},
            {"20141013", "体育の日"},
            {"20141103", "文化の日"},
            {"20141123", "勤労感謝の日"},
            {"20141124", "振替休日"},
            {"20141223", "天皇誕生日"},
            {"20150101", "元日"},
            {"20150112", "成人の日"},
            {"20150211", "建国記念の日"},
            {"20150321", "春分の日"},
            {"20150429", "昭和の日"},
            {"20150503", "憲法記念日"},
            {"20150504", "みどりの日"},
            {"20150505", "こどもの日"},
            {"20150506", "振替休日"},
            {"20150720", "海の日"},
            {"20150921", "敬老の日"},
            {"20150922", "国民の休日"},
            {"20150923", "秋分の日"},
            {"20151012", "体育の日"},
            {"20151103", "文化の日"},
            {"20151123", "勤労感謝の日"},
            {"20151223", "天皇誕生日"},
            {"20160101", "元日"},
            {"20160111", "成人の日"},
            {"20160211", "建国記念の日"},
            {"20160320", "春分の日"},
            {"20160321", "振替休日"},
            {"20160429", "昭和の日"},
            {"20160503", "憲法記念日"},
            {"20160504", "みどりの日"},
            {"20160505", "こどもの日"},
            {"20160718", "海の日"},
            {"20160811", "山の日"},
            {"20160919", "敬老の日"},
            {"20160922", "秋分の日"},
            {"20161010", "体育の日"},
            {"20161103", "文化の日"},
            {"20161123", "勤労感謝の日"},
            {"20161223", "天皇誕生日"},
            {"20170101", "元日"},
            {"20170102", "振替休日"},
            {"20170109", "成人の日"},
            {"20170211", "建国記念の日"},
            {"20170320", "春分の日"},
            {"20170429", "昭和の日"},
            {"20170503", "憲法記念日"},
            {"20170504", "みどりの日"},
            {"20170505", "こどもの日"},
            {"20170717", "海の日"},
            {"20170811", "山の日"},
            {"20170918", "敬老の日"},
            {"20170923", "秋分の日"},
            {"20171009", "体育の日"},
            {"20171103", "文化の日"},
            {"20171123", "勤労感謝の日"},
            {"20171223", "天皇誕生日"},
            {"20180101", "元日"},
            {"20180108", "成人の日"},
            {"20180211", "建国記念の日"},
            {"20180212", "振替休日"},
            {"20180321", "春分の日"},
            {"20180429", "昭和の日"},
            {"20180430", "振替休日"},
            {"20180503", "憲法記念日"},
            {"20180504", "みどりの日"},
            {"20180505", "こどもの日"},
            {"20180716", "海の日"},
            {"20180811", "山の日"},
            {"20180917", "敬老の日"},
            {"20180923", "秋分の日"},
            {"20180924", "振替休日"},
            {"20181008", "体育の日"},
            {"20181103", "文化の日"},
            {"20181123", "勤労感謝の日"},
            {"20181223", "天皇誕生日"},
            {"20181224", "振替休日"},
            {"20190101", "元日"},
            {"20190114", "成人の日"},
            {"20190211", "建国記念の日"},
            {"20190321", "春分の日"},
            {"20190429", "昭和の日"},
            {"20190430", "休日"},
            {"20190501", "新天皇即位"},
            {"20190502", "休日"},
            {"20190503", "憲法記念日"},
            {"20190504", "みどりの日"},
            {"20190505", "こどもの日"},
            {"20190506", "振替休日"},
            {"20190715", "海の日"},
            {"20190811", "山の日"},
            {"20190812", "振替休日"},
            {"20190916", "敬老の日"},
            {"20190923", "秋分の日"},
            {"20191014", "体育の日"},
            {"20191022", "即位礼正殿の儀"},
            {"20191103", "文化の日"},
            {"20191104", "振替休日"},
            {"20191123", "勤労感謝の日"},
            {"20191223", "天皇誕生日"},
            {"20200101", "元日"},
            {"20200113", "成人の日"},
            {"20200211", "建国記念の日"},
            {"20200320", "春分の日"},
            {"20200429", "昭和の日"},
            {"20200503", "憲法記念日"},
            {"20200504", "みどりの日"},
            {"20200505", "こどもの日"},
            {"20200506", "振替休日"},
            {"20200720", "海の日"},
            {"20200811", "山の日"},
            {"20200921", "敬老の日"},
            {"20200922", "秋分の日"},
            {"20201012", "体育の日"},
            {"20201103", "文化の日"},
            {"20201123", "勤労感謝の日"},
            {"20201223", "天皇誕生日"},
            {"20210101", "元日"},
            {"20210111", "成人の日"},
            {"20210211", "建国記念の日"},
            {"20210320", "春分の日"},
            {"20210429", "昭和の日"},
            {"20210503", "憲法記念日"},
            {"20210504", "みどりの日"},
            {"20210505", "こどもの日"},
            {"20210719", "海の日"},
            {"20210811", "山の日"},
            {"20210920", "敬老の日"},
            {"20210923", "秋分の日"},
            {"20211011", "体育の日"},
            {"20211103", "文化の日"},
            {"20211123", "勤労感謝の日"},
            {"20211223", "天皇誕生日"},
            {"20220101", "元日"},
            {"20220110", "成人の日"},
            {"20220211", "建国記念の日"},
            {"20220321", "春分の日"},
            {"20220429", "昭和の日"},
            {"20220503", "憲法記念日"},
            {"20220504", "みどりの日"},
            {"20220505", "こどもの日"},
            {"20220718", "海の日"},
            {"20220811", "山の日"},
            {"20220919", "敬老の日"},
            {"20220923", "秋分の日"},
            {"20221010", "体育の日"},
            {"20221103", "文化の日"},
            {"20221123", "勤労感謝の日"},
            {"20221223", "天皇誕生日"},
            {"20230101", "元日"},
            {"20230102", "振替休日"},
            {"20230109", "成人の日"},
            {"20230211", "建国記念の日"},
            {"20230321", "春分の日"},
            {"20230429", "昭和の日"},
            {"20230503", "憲法記念日"},
            {"20230504", "みどりの日"},
            {"20230505", "こどもの日"},
            {"20230717", "海の日"},
            {"20230811", "山の日"},
            {"20230918", "敬老の日"},
            {"20230923", "秋分の日"},
            {"20231009", "体育の日"},
            {"20231103", "文化の日"},
            {"20231123", "勤労感謝の日"},
            {"20231223", "天皇誕生日"},
            {"20240101", "元日"},
            {"20240108", "成人の日"},
            {"20240211", "建国記念の日"},
            {"20240212", "振替休日"},
            {"20240320", "春分の日"},
            {"20240429", "昭和の日"},
            {"20240503", "憲法記念日"},
            {"20240504", "みどりの日"},
            {"20240505", "こどもの日"},
            {"20240506", "振替休日"},
            {"20240715", "海の日"},
            {"20240811", "山の日"},
            {"20240812", "振替休日"},
            {"20240916", "敬老の日"},
            {"20240922", "秋分の日"},
            {"20240923", "振替休日"},
            {"20241014", "体育の日"},
            {"20241103", "文化の日"},
            {"20241104", "振替休日"},
            {"20241123", "勤労感謝の日"},
            {"20241223", "天皇誕生日"},
            {"20250101", "元日"},
            {"20250113", "成人の日"},
            {"20250211", "建国記念の日"},
            {"20250320", "春分の日"},
            {"20250429", "昭和の日"},
            {"20250503", "憲法記念日"},
            {"20250504", "みどりの日"},
            {"20250505", "こどもの日"},
            {"20250506", "振替休日"},
            {"20250721", "海の日"},
            {"20250811", "山の日"},
            {"20250915", "敬老の日"},
            {"20250923", "秋分の日"},
            {"20251013", "体育の日"},
            {"20251103", "文化の日"},
            {"20251123", "勤労感謝の日"},
            {"20251124", "振替休日"},
            {"20251223", "天皇誕生日"},
            {"20260101", "元日"},
            {"20260112", "成人の日"},
            {"20260211", "建国記念の日"},
            {"20260320", "春分の日"},
            {"20260429", "昭和の日"},
            {"20260503", "憲法記念日"},
            {"20260504", "みどりの日"},
            {"20260505", "こどもの日"},
            {"20260506", "振替休日"},
            {"20260720", "海の日"},
            {"20260811", "山の日"},
            {"20260921", "敬老の日"},
            {"20260922", "国民の休日"},
            {"20260923", "秋分の日"},
            {"20261012", "体育の日"},
            {"20261103", "文化の日"},
            {"20261123", "勤労感謝の日"},
            {"20261223", "天皇誕生日"},
            {"20270101", "元日"},
            {"20270111", "成人の日"},
            {"20270211", "建国記念の日"},
            {"20270321", "春分の日"},
            {"20270322", "振替休日"},
            {"20270429", "昭和の日"},
            {"20270503", "憲法記念日"},
            {"20270504", "みどりの日"},
            {"20270505", "こどもの日"},
            {"20270719", "海の日"},
            {"20270811", "山の日"},
            {"20270920", "敬老の日"},
            {"20270923", "秋分の日"},
            {"20271011", "体育の日"},
            {"20271103", "文化の日"},
            {"20271123", "勤労感謝の日"},
            {"20271223", "天皇誕生日"},
            {"20280101", "元日"},
            {"20280110", "成人の日"},
            {"20280211", "建国記念の日"},
            {"20280320", "春分の日"},
            {"20280429", "昭和の日"},
            {"20280503", "憲法記念日"},
            {"20280504", "みどりの日"},
            {"20280505", "こどもの日"},
            {"20280717", "海の日"},
            {"20280811", "山の日"},
            {"20280918", "敬老の日"},
            {"20280922", "秋分の日"},
            {"20281009", "体育の日"},
            {"20281103", "文化の日"},
            {"20281123", "勤労感謝の日"},
            {"20281223", "天皇誕生日"},
            {"20290101", "元日"},
            {"20290108", "成人の日"},
            {"20290211", "建国記念の日"},
            {"20290212", "振替休日"},
            {"20290320", "春分の日"},
            {"20290429", "昭和の日"},
            {"20290430", "振替休日"},
            {"20290503", "憲法記念日"},
            {"20290504", "みどりの日"},
            {"20290505", "こどもの日"},
            {"20290716", "海の日"},
            {"20290811", "山の日"},
            {"20290917", "敬老の日"},
            {"20290923", "秋分の日"},
            {"20290924", "振替休日"},
            {"20291008", "体育の日"},
            {"20291103", "文化の日"},
            {"20291123", "勤労感謝の日"},
            {"20291223", "天皇誕生日"},
            {"20291224", "振替休日"},
            {"20300101", "元日"},
            {"20300114", "成人の日"},
            {"20300211", "建国記念の日"},
            {"20300320", "春分の日"},
            {"20300429", "昭和の日"},
            {"20300503", "憲法記念日"},
            {"20300504", "みどりの日"},
            {"20300505", "こどもの日"},
            {"20300506", "振替休日"},
            {"20300715", "海の日"},
            {"20300811", "山の日"},
            {"20300812", "振替休日"},
            {"20300916", "敬老の日"},
            {"20300923", "秋分の日"},
            {"20301014", "体育の日"},
            {"20301103", "文化の日"},
            {"20301104", "振替休日"},
            {"20301123", "勤労感謝の日"},
            {"20301223", "天皇誕生日"},
            {"20310101", "元日"},
    };
}

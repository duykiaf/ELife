package t3h.android.elife.helper;

import java.util.ArrayList;
import java.util.List;

import t3h.android.elife.models.DropdownItem;

public class LanguagesDropdownHelper {
    public static List<DropdownItem> languagesDropdown() {
        List<DropdownItem> item = new ArrayList<>();
        item.add(new DropdownItem("", "Choose translate language"));
        item.add(new DropdownItem("af", "Afrikaans"));
        item.add(new DropdownItem("ar", "Arabic"));
        item.add(new DropdownItem("be", "Belarusian"));
        item.add(new DropdownItem("bg", "Bulgarian"));
        item.add(new DropdownItem("bn", "Bengali"));
        item.add(new DropdownItem("ca", "Catalan"));
        item.add(new DropdownItem("cs", "Czech"));
        item.add(new DropdownItem("cy", "Welsh"));
        item.add(new DropdownItem("da", "Danish"));
        item.add(new DropdownItem("de", "German"));
        item.add(new DropdownItem("el", "Greek"));
        item.add(new DropdownItem("eo", "Esperanto"));
        item.add(new DropdownItem("es", "Spanish"));
        item.add(new DropdownItem("et", "Estonian"));
        item.add(new DropdownItem("fa", "Persian"));
        item.add(new DropdownItem("fi", "Finnish"));
        item.add(new DropdownItem("fr", "French"));
        item.add(new DropdownItem("ga", "Irish"));
        item.add(new DropdownItem("gl", "Galician"));
        item.add(new DropdownItem("gu", "Gujarati"));
        item.add(new DropdownItem("he", "Hebrew"));
        item.add(new DropdownItem("hi", "Hindi"));
        item.add(new DropdownItem("hr", "Croatian"));
        item.add(new DropdownItem("ht", "Haitian"));
        item.add(new DropdownItem("hu", "Hungarian"));
        item.add(new DropdownItem("id", "Indonesian"));
        item.add(new DropdownItem("is", "Icelandic"));
        item.add(new DropdownItem("it", "Italian"));
        item.add(new DropdownItem("ja", "Japanese"));
        item.add(new DropdownItem("ka", "Georgian"));
        item.add(new DropdownItem("kn", "Kannada"));
        item.add(new DropdownItem("ko", "Korean"));
        item.add(new DropdownItem("lt", "Lithuanian"));
        item.add(new DropdownItem("lv", "Latvian"));
        item.add(new DropdownItem("mk", "Macedonian"));
        item.add(new DropdownItem("mr", "Marathi"));
        item.add(new DropdownItem("ms", "Malay"));
        item.add(new DropdownItem("mt", "Maltese"));
        item.add(new DropdownItem("nl", "Dutch"));
        item.add(new DropdownItem("no", "Norwegian"));
        item.add(new DropdownItem("pl", "Polish"));
        item.add(new DropdownItem("pt", "Portuguese"));
        item.add(new DropdownItem("ro", "Romanian"));
        item.add(new DropdownItem("ru", "Russian"));
        item.add(new DropdownItem("sk", "Slovak"));
        item.add(new DropdownItem("sl", "Slovenian"));
        item.add(new DropdownItem("sq", "Albanian"));
        item.add(new DropdownItem("sv", "Swedish"));
        item.add(new DropdownItem("sw", "Swahili"));
        item.add(new DropdownItem("ta", "Tamil"));
        item.add(new DropdownItem("te", "Telugu"));
        item.add(new DropdownItem("th", "Thai"));
        item.add(new DropdownItem("tl", "Tagalog"));
        item.add(new DropdownItem("tr", "Turkish"));
        item.add(new DropdownItem("uk", "Ukrainian"));
        item.add(new DropdownItem("ur", "Urdu"));
        item.add(new DropdownItem("vi", "Vietnamese"));
        item.add(new DropdownItem("zh", "Chinese"));
        return item;
    }
}

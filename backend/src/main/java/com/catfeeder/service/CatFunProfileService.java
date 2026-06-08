package com.catfeeder.service;

import com.catfeeder.entity.Cat;
import com.catfeeder.entity.CatCapture;
import com.catfeeder.repository.CatCaptureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CatFunProfileService {

    @Autowired
    private CatCaptureRepository catCaptureRepository;

    private static final Map<String, String[]> COLOR_NAMES = new HashMap<>();
    private static final Map<String, String[]> BODY_NAMES = new HashMap<>();
    private static final Map<String, String[]> PERSONALITY_NAMES = new HashMap<>();
    private static final String[] TITLE_PREFIXES = {
            "暗夜", "花园", "小区", "流浪", "神秘", "传说", "闪电", "影",
            "胖", "瘦", "大", "小", "超级", "无敌", "可爱", "傲娇"
    };
    private static final String[] TITLE_SUFFIXES = {
            "刺客", "骑士", "公主", "王子", "大王", "将军", "侠客", "隐士",
            "精灵", "仙子", "魔王", "护法", "使者", "猎手", "守护者", "美食家"
    };

    static {
        COLOR_NAMES.put("orange", new String[]{"大橘", "橘子", "橙橙", "小橘", "金桔", "芒果", "南瓜", "黄油"});
        COLOR_NAMES.put("black", new String[]{"小黑", "煤球", "黑豆", "黑夜", "墨墨", "黑炭", "黑豹", "影子"});
        COLOR_NAMES.put("white", new String[]{"小白", "雪球", "奶糖", "云朵", "棉花", "白雪", "米粒", "蛋白"});
        COLOR_NAMES.put("gray", new String[]{"灰灰", "小灰", "烟灰", "银灰", "云雾", "石头", "灰豆", "鼠鼠"});
        COLOR_NAMES.put("calico", new String[]{"三花", "花花", "彩彩", "斑斓", "云锦", "花卷", "绣球", "拼拼"});
        COLOR_NAMES.put("tabby", new String[]{"虎斑", "纹纹", "条总", "斑斑", "虎皮", "花纹", "豹纹", "条纹"});
        COLOR_NAMES.put("black_white", new String[]{"奶牛", "黑白", "牛牛", "奥利奥", "斑斑", "太极", "熊猫", "斑点"});
        COLOR_NAMES.put("brown", new String[]{"巧克力", "可可", "栗子", "咖啡", "棕棕", "焦糖", "奶茶", "榛子"});
    }

    static {
        BODY_NAMES.put("slim", new String[]{"苗条", "纤细", "轻盈", "灵动", "瘦猴", "竹竿", "闪电", "疾风"});
        BODY_NAMES.put("normal", new String[]{"标准", "匀称", "适中", "健美", "普通", "正常", "标准", "均衡"});
        BODY_NAMES.put("fat", new String[]{"胖橘", "圆滚滚", "肉球", "胖胖", "肥宅", "团子", "气球", "年糕"});
        BODY_NAMES.put("chubby", new String[]{"圆滚滚", "胖乎乎", "肉嘟嘟", "软糯", "小胖", "团子", "年糕", "泡芙"});
        BODY_NAMES.put("muscular", new String[]{"肌肉", "健壮", "猛男", "大力", "壮汉", "筋肉", "力量", "刚猛"});
    }

    static {
        PERSONALITY_NAMES.put("shy", new String[]{"害羞", "胆小", "怕人", "躲猫猫", "羞怯", "腼腆", "怕生", "娇羞"});
        PERSONALITY_NAMES.put("bold", new String[]{"大胆", "勇敢", "勇猛", "无畏", "嚣张", "霸道", "大哥", "强势"});
        PERSONALITY_NAMES.put("lazy", new String[]{"懒惰", "爱睡", "宅猫", "佛系", "躺平", "摆烂", "咸鱼", "睡神"});
        PERSONALITY_NAMES.put("greedy", new String[]{"贪吃", "吃货", "大胃王", "馋猫", "美食家", "贪嘴", "干饭", "饭桶"});
        PERSONALITY_NAMES.put("friendly", new String[]{"亲人", "友善", "粘人", "撒娇", "友好", "温柔", "乖巧", "贴心"});
        PERSONALITY_NAMES.put("mysterious", new String[]{"神秘", "高冷", "难以捉摸", "影子", "神秘人", "莫测", "隐士", "游侠"});
    }

    private static final String[] STORY_TEMPLATES_FOOD = {
            "小区里有个传说，每当夜深人静时，{name}就会准时出现在喂食点，用它那双{eye}色的眼睛盯着你，仿佛在说：'还不快给本喵上饭！'",
            "据老居民说，{name}是这条街的干饭王，曾经在三分钟内吃完了一整碗猫粮，连渣都不剩。",
            "传说{name}有个秘密基地，里面藏着它从各个喂食点搜集来的零食，据说已经堆满了半个墙角。",
            "小区喂养机的数据显示，{name}是全小区最勤奋的干饭猫，风雨无阻，每天至少光顾三次。",
            "每到饭点，{name}总是第一个到达现场，它会用尾巴轻轻拍打喂食机，好像在说：'快点，本喵饿了！'"
    };

    private static final String[] STORY_TEMPLATES_NIGHT = {
            "夜半三更，万籁俱寂，{name}便开始了它的夜间巡逻。从1号楼到5号楼，每一个角落都留下了它的身影。",
            "据夜归的居民说，常常在凌晨两点看到{name}端坐在墙头，像一位{title}俯瞰着自己的领地。",
            "深夜的小区是{name}的天下。白天它躲起来睡觉，夜幕降临时，它就化身为{title}，四处游荡。",
            "保安大叔说，{name}是小区的夜间巡逻队队长，每晚准时打卡，比闹钟还准。",
            "有人说，{name}白天是只普通的流浪猫，到了晚上就会变成{title}，守护着整个小区的和平。"
    };

    private static final String[] STORY_TEMPLATES_SHY = {
            "{name}是小区里最神秘的猫咪，很少有人见过它的真面目。据说只有在夜深人静的时候，它才会出来觅食。",
            "如果你想见到{name}，得有足够的耐心。它总是躲在灌木丛后面，用那双{eye}色的眼睛偷偷观察着你。",
            "传说{name}是从别的地方流浪来的，因为受过人类的伤害，所以总是躲得远远的。",
            "虽然{name}很怕人，但它每天都会来吃饭。志愿者们说，它总是等别的猫吃完了，才悄悄走过来。",
            "小区里有个温柔的志愿者，每天都会给{name}留一份食物，放在它常去的角落，希望有一天它能放下戒备。"
    };

    private static final String[] STORY_TEMPLATES_FRIENDLY = {
            "{name}是小区的明星猫，大人小孩都喜欢它。它总是懒洋洋地躺在草坪上，任人抚摸。",
            "只要你手里有吃的，{name}就会立刻凑过来，用头蹭你的腿，发出呼噜呼噜的声音，特别粘人。",
            "据说{name}以前是只家猫，后来被主人遗弃了。但它依然相信人类，愿意亲近每一个对它好的人。",
            "小区的孩子们最喜欢{name}了，每天放学都要来看它，给它带零食，跟它说悄悄话。",
            "{name}是个撒娇高手，只要它用那双大眼睛看着你，再喵几声，你就会忍不住把手里的好吃的都给它。"
    };

    private static final String[] STORY_TEMPLATES_BOSS = {
            "{name}是这片区域的猫老大，所有的猫咪都得听它的。每次吃饭，都是它先吃，别的猫才敢靠近。",
            "传说{name}曾经打败了三只入侵的野猫，保卫了小区的猫咪领地，从此成为了小区的守护者。",
            "别看{name}平时懒洋洋的，真要是有别的猫来抢地盘，它可是毫不含糊，上去就是一顿喵喵拳。",
            "小区里流传着{name}的传说，据说它曾经一个打五个，把入侵的野猫打得落花流水。",
            "{name}是小区猫咪们的老大哥，哪里有矛盾，只要它一出面，立刻就平息了。"
    };

    private static final String[] STORY_ENDINGS = {
            "这就是{name}的故事，一只普通又不平凡的流浪猫。",
            "虽然{name}只是一只流浪猫，但它也有自己的生活和故事。",
            "如果你在小区里遇到了{name}，记得跟它打个招呼哦～",
            "每一只流浪猫都有自己的名字和故事，{name}也不例外。",
            "希望{name}能在小区里一直幸福快乐地生活下去。"
    };

    private static final String[] CATCHPHRASES = {
            "干饭不积极，思想有问题！",
            "喵生苦短，必须性感。",
            "本喵的地盘，本喵说了算。",
            "今天也是努力干饭的一天！",
            "世界和平，全靠本喵。",
            "睡饱了才有力气减肥。",
            "每一只流浪猫都是折翼的天使。",
            "用我双爪，守护家园。",
            "朕的江山，朕来守护。",
            "今天也是被自己帅醒的一天。"
    };

    public Map<String, Object> generateFunProfile(Cat cat) {
        Map<String, Object> profile = new HashMap<>();

        String funName = generateFunName(cat);
        String funTitle = generateFunTitle(cat);
        String story = generateStory(cat);
        String catchphrase = randomPick(CATCHPHRASES);
        List<String> tags = generateTags(cat);
        Map<String, Object> stats = generateFunStats(cat);

        profile.put("funName", funName);
        profile.put("funTitle", funTitle);
        profile.put("story", story);
        profile.put("catchphrase", catchphrase);
        profile.put("tags", tags);
        profile.put("stats", stats);
        profile.put("catId", cat.getId());
        profile.put("catCode", cat.getCatCode());
        profile.put("originalName", cat.getName());

        return profile;
    }

    private String generateFunName(Cat cat) {
        List<String> candidates = new ArrayList<>();

        String[] colorNames = COLOR_NAMES.getOrDefault(cat.getFurColor(), new String[]{"小猫咪", "猫猫", "咪咪", "喵酱"});
        candidates.addAll(Arrays.asList(colorNames));

        String[] bodyNames = BODY_NAMES.getOrDefault(cat.getBodyType(), new String[]{});
        candidates.addAll(Arrays.asList(bodyNames));

        if (candidates.isEmpty()) {
            candidates.add("小喵");
            candidates.add("咪咪");
            candidates.add("团子");
            candidates.add("雪球");
        }

        return randomPick(candidates.toArray(new String[0]));
    }

    private String generateFunTitle(Cat cat) {
        String prefix = randomPick(TITLE_PREFIXES);
        String suffix = randomPick(TITLE_SUFFIXES);

        if (cat.getVisitCount() != null && cat.getVisitCount() > 100) {
            prefix = "传说";
        } else if (cat.getVisitCount() != null && cat.getVisitCount() > 50) {
            prefix = "小区";
        }

        if ("fat".equals(cat.getBodyType()) || "chubby".equals(cat.getBodyType())) {
            if (Math.random() > 0.5) {
                return prefix + suffix;
            } else {
                return randomPick(new String[]{"干饭王", "美食家", "胖总", "团子大人", "圆滚滚大侠"});
            }
        }

        if ("black".equals(cat.getFurColor())) {
            if (Math.random() > 0.6) {
                return "暗夜" + randomPick(new String[]{"刺客", "骑士", "猎手", "影武者"});
            }
        }

        if ("orange".equals(cat.getFurColor())) {
            if (Math.random() > 0.6) {
                return randomPick(new String[]{"橘座大人", "大橘为重", "橘老板", "胖橘将军"});
            }
        }

        return prefix + suffix;
    }

    private String generateStory(Cat cat) {
        String personality = detectPersonality(cat);
        String[] templates;

        switch (personality) {
            case "night_cat":
                templates = STORY_TEMPLATES_NIGHT;
                break;
            case "shy":
                templates = STORY_TEMPLATES_SHY;
                break;
            case "friendly":
                templates = STORY_TEMPLATES_FRIENDLY;
                break;
            case "boss":
                templates = STORY_TEMPLATES_BOSS;
                break;
            case "foodie":
            default:
                templates = STORY_TEMPLATES_FOOD;
                break;
        }

        String story = randomPick(templates);
        String ending = randomPick(STORY_ENDINGS);

        String name = cat.getName();
        String title = generateFunTitle(cat);
        String eyeColor = formatEyeColor(cat.getEyeColor());

        story = story.replace("{name}", name)
                .replace("{title}", title)
                .replace("{eye}", eyeColor);

        ending = ending.replace("{name}", name);

        return story + "\n\n" + ending;
    }

    private String detectPersonality(Cat cat) {
        List<CatCapture> captures = catCaptureRepository.findByCatIdOrderByCaptureTimeDesc(cat.getId());

        if (captures.isEmpty()) {
            return "foodie";
        }

        int nightVisits = 0;
        int totalVisits = Math.min(captures.size(), 30);

        for (int i = 0; i < totalVisits; i++) {
            CatCapture capture = captures.get(i);
            int hour = capture.getCaptureTime().getHour();
            if (hour >= 22 || hour < 6) {
                nightVisits++;
            }
        }

        boolean isNightCat = totalVisits > 5 && (double) nightVisits / totalVisits > 0.6;

        if (isNightCat) {
            return "night_cat";
        }

        if (cat.getVisitCount() != null && cat.getVisitCount() > 80) {
            return "boss";
        }

        if ("fat".equals(cat.getBodyType()) || "chubby".equals(cat.getBodyType())) {
            return "foodie";
        }

        if ("slim".equals(cat.getBodyType())) {
            if (cat.getVisitCount() != null && cat.getVisitCount() < 20) {
                return "shy";
            }
        }

        Random random = new Random();
        String[] personalities = {"foodie", "friendly", "boss", "shy"};
        return personalities[random.nextInt(personalities.length)];
    }

    private List<String> generateTags(Cat cat) {
        List<String> tags = new ArrayList<>();

        tags.add(formatFurColor(cat.getFurColor()));

        if ("fat".equals(cat.getBodyType())) {
            tags.add("胖乎乎");
            tags.add("干饭王");
        } else if ("slim".equals(cat.getBodyType())) {
            tags.add("身材好");
            tags.add("敏捷");
        } else {
            tags.add("身材标准");
        }

        if (cat.getIsNeutered() != null && cat.getIsNeutered()) {
            tags.add("已绝育");
        }

        if (cat.getVisitCount() != null) {
            if (cat.getVisitCount() > 100) {
                tags.add("常客");
                tags.add("老住户");
            } else if (cat.getVisitCount() < 10) {
                tags.add("新面孔");
            }
        }

        if (cat.getIsNew() != null && cat.getIsNew()) {
            tags.add("新发现");
        }

        String personality = detectPersonality(cat);
        switch (personality) {
            case "night_cat":
                tags.add("夜猫子");
                tags.add("神秘");
                break;
            case "shy":
                tags.add("怕人");
                tags.add("胆小");
                break;
            case "friendly":
                tags.add("亲人");
                tags.add("粘人");
                break;
            case "boss":
                tags.add("大佬");
                tags.add("领地意识强");
                break;
            case "foodie":
                tags.add("吃货");
                tags.add("爱吃");
                break;
        }

        if (tags.size() > 6) {
            return tags.subList(0, 6);
        }

        return tags;
    }

    private Map<String, Object> generateFunStats(Cat cat) {
        Map<String, Object> stats = new HashMap<>();

        int visitCount = cat.getVisitCount() != null ? cat.getVisitCount() : 0;
        stats.put("visitCount", visitCount);
        stats.put("riceBowlLevel", Math.min(visitCount / 10, 100));
        stats.put("moeValue", new Random().nextInt(30) + 70);
        stats.put("mysteryValue", new Random().nextInt(40) + 30);

        if (cat.getFirstSeenTime() != null) {
            long days = ChronoUnit.DAYS.between(cat.getFirstSeenTime(), LocalDateTime.now());
            stats.put("daysInCommunity", (int) days);
        } else {
            stats.put("daysInCommunity", 0);
        }

        if (cat.getLastSeenTime() != null) {
            long hours = ChronoUnit.HOURS.between(cat.getLastSeenTime(), LocalDateTime.now());
            stats.put("lastSeenHoursAgo", (int) hours);
        } else {
            stats.put("lastSeenHoursAgo", -1);
        }

        return stats;
    }

    private String formatFurColor(String color) {
        Map<String, String> colorMap = new HashMap<>();
        colorMap.put("orange", "橘色");
        colorMap.put("black", "黑色");
        colorMap.put("white", "白色");
        colorMap.put("gray", "灰色");
        colorMap.put("calico", "三花");
        colorMap.put("tabby", "虎斑");
        colorMap.put("black_white", "黑白");
        colorMap.put("brown", "棕色");
        return colorMap.getOrDefault(color, color != null ? color : "未知");
    }

    private String formatEyeColor(String color) {
        Map<String, String> colorMap = new HashMap<>();
        colorMap.put("yellow", "金黄");
        colorMap.put("green", "碧绿");
        colorMap.put("blue", "湛蓝");
        colorMap.put("brown", "琥珀");
        colorMap.put("copper", "铜色");
        colorMap.put("odd", "双色");
        return colorMap.getOrDefault(color, color != null ? color : "神秘");
    }

    private <T> T randomPick(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        Random random = new Random();
        return array[random.nextInt(array.length)];
    }
}

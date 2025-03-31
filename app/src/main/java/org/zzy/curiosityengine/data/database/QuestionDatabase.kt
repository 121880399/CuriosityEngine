package org.zzy.curiosityengine.data.database

/**
 * 问题数据库类
 * 用于存储预设问题并提供随机获取问题的功能
 */
class QuestionDatabase {
    
    // 科学类问题 - 更适合4-14岁儿童的好奇心
    private val scienceQuestions = listOf(
        "为什么天空是蓝色的？",
        "恐龙是怎么灭绝的？",
        "植物是怎么长大的？",
        "为什么会有四季变化？",
        "彩虹是怎么形成的？",
        "为什么海水是咸的？",
        "为什么地球是圆的？",
        "为什么我们会打嗝？",
        "为什么有些人会打呼噜？",
        "为什么我们会做梦？",
        "为什么我们的指纹都不一样？",
        "为什么猫咪会用舌头洗脸？",
        "为什么下雨时会有雷声？",
        "为什么我们能在镜子里看到自己？",
        "为什么肥皂泡是圆的？",
        "为什么我们会打喷嚏？",
        "为什么有些动物会冬眠？",
        "为什么我们的头发会变白？",
        "为什么冰会融化？",
        "为什么我们能闻到香味？"
    )
    
    // 历史类问题 - 更适合4-14岁儿童的好奇心
    private val historyQuestions = listOf(
        "古埃及金字塔是如何建造的？",
        "长城是什么时候建造的？",
        "谁发明了印刷术？",
        "恐龙在地球上生活了多久？",
        "古代小朋友玩什么玩具？",
        "为什么我们要过春节？",
        "第一辆汽车是什么样子的？",
        "古代人是怎么点灯的？",
        "为什么我们要上学？",
        "第一部电影是什么样的？",
        "古代人是怎么寄信的？",
        "为什么有些城市有城墙？",
        "中国的指南针是怎么发明的？",
        "第一架飞机是怎么飞起来的？",
        "古代人是怎么记时间的？"
    )
    
    // 技术类问题 - 更适合4-14岁儿童的好奇心
    private val technologyQuestions = listOf(
        "机器人是怎么动起来的？",
        "手机是怎么知道我在说话的？",
        "电脑是怎么记住东西的？",
        "为什么飞机能飞在天上？",
        "电是怎么到我们家的？",
        "为什么冰箱能让东西变冷？",
        "太阳能是怎么变成电的？",
        "为什么我们能在电视上看到人说话？",
        "互联网是怎么让我们看到图片的？",
        "为什么GPS能知道我们在哪里？",
        "为什么收音机能听到声音？",
        "为什么灯泡会发光？",
        "为什么我们能用手机拍照？",
        "为什么电梯能上下移动？",
        "为什么我们能在水下呼吸？"
    )
    
    // 所有问题的集合
    private val allQuestions = scienceQuestions + historyQuestions + technologyQuestions
    
    /**
     * 获取随机问题列表
     * @param count 需要获取的问题数量
     * @param category 问题类别，可选值："science"、"history"、"technology"、"all"(默认)
     * @return 随机问题列表
     */
    fun getRandomQuestions(count: Int = 3, category: String = "all"): List<String> {
        val questionPool = when (category.lowercase()) {
            "science" -> scienceQuestions
            "history" -> historyQuestions
            "technology" -> technologyQuestions
            else -> allQuestions
        }
        
        return if (questionPool.size <= count) {
            questionPool.shuffled()
        } else {
            questionPool.shuffled().take(count)
        }
    }
}
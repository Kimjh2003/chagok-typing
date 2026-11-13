package dev.typing.practice.core

/** Allocate prompt collections once; existing paragraph prompts remain for restored sessions. */
object PromptRepository {
    private val korean = arrayOf(
        arrayOf("바람", "여유", "키보드", "햇살", "구름", "산책", "노을", "향기", "기억", "시작", "온기", "리듬", "도서관", "반짝임", "물결", "나무", "여행", "하늘", "책갈피", "고요"),
        arrayOf("오늘은 나만의 속도로 한 걸음 나아간다.", "작은 연습이 모여 자연스러운 움직임이 된다.", "창밖으로 부드러운 바람이 불어온다.", "따뜻한 차 한 잔이 마음을 편안하게 한다.", "천천히 걸어도 풍경은 충분히 아름답다.", "익숙한 노래가 조용한 오후를 채운다.", "오늘의 작은 발견을 오래 기억하고 싶다.", "비가 그친 뒤 맑은 하늘이 모습을 드러냈다.", "한 글자씩 이어 가다 보면 문장이 완성된다.", "가벼운 산책은 새로운 생각을 불러온다.", "서두르지 않아도 괜찮아, 지금도 잘하고 있어.", "창가에 놓인 식물이 햇빛을 향해 자란다.", "좋아하는 이야기를 읽으며 하루를 마무리한다.", "작은 습관이 쌓이면 어느새 큰 변화가 된다.", "잠시 쉬어 가는 시간도 소중한 연습이다."),
        arrayOf("아침 햇살이 창문 사이로 들어온다. 따뜻한 차 한 잔을 곁에 두고 오늘의 연습을 시작한다. 서두르지 않아도 괜찮다.", "새로운 문장을 천천히 읽어 본다. 손끝의 움직임에 집중하며 글자를 이어 간다. 작은 변화가 쌓여 내일의 실력이 된다."),
    )
    private val english = arrayOf(
        arrayOf("keyboard", "rhythm", "practice", "sunlight", "breeze", "coffee", "garden", "journey", "quiet", "horizon", "cloud", "wonder", "morning", "lantern", "pencil", "rainbow", "window", "melody", "focus", "gentle", "apple", "bridge", "butterfly", "candle", "chapter", "cherry", "circle", "courage", "dream", "evening", "feather", "forest", "friend", "glimmer", "harbor", "island", "jasmine", "lavender", "library", "meadow", "mountain", "notebook", "ocean", "pebble", "picnic", "puzzle", "river", "season", "sparkle", "sunflower", "thunder", "whisper", "wildflower", "winter", "yesterday", "zephyr"),
        arrayOf("Small steps make steady progress.", "Find your rhythm and enjoy the practice.", "A quiet morning brings a fresh start.", "The warm sunlight falls across the room.", "A gentle breeze moves through the trees.", "Every new word is a chance to learn.", "Take your time and find a comfortable pace.", "A good song can brighten an ordinary day.", "The rain stopped, and the sky turned blue.", "A little practice can build lasting confidence.", "Keep going, one sentence at a time.", "A cup of tea makes the afternoon calm.", "The best ideas often arrive on a walk.", "Make room for rest as well as progress.", "Today is a lovely day to try again.", "A bright moon rises above the hills.", "The library is quiet on a rainy afternoon.", "Fresh bread fills the kitchen with warmth.", "A small lantern glows beside the window.", "Listen to the waves along the shore.", "The garden wakes up with the spring rain.", "We can find beauty in ordinary moments.", "A curious mind is always ready to explore.", "Take a deep breath and begin once more.", "The leaves turn gold as autumn arrives.", "A friendly smile can make a day brighter.", "Write a little, then take a gentle break.", "Soft clouds drift across the evening sky.", "There is no need to rush the journey.", "The sound of rain makes the room feel cozy.", "Every page brings a new story to life.", "A quiet walk can clear a busy mind.", "Keep a little space for your dreams.", "The first snow covers the rooftops in white.", "A warm light shines at the end of the street.", "Your own pace is a good place to start."),
        arrayOf("Take a moment to find your own pace. Let each word flow naturally into the next. A little practice every day can make a difference.", "The morning light falls across the desk. You take a breath and begin a new sentence. There is always time for one more try."),
    )
    private fun options(language: String, mode: String): Array<String> {
        val kind = when (mode) { "단어" -> 0; "문장" -> 1; else -> 2 }
        return (if (language == "한국어") korean else english)[kind]
    }
    fun get(language: String, mode: String, sample: Int): String {
        val opts = options(language, mode)
        return opts[Math.floorMod(sample, opts.size)]
    }
    fun size(language: String, mode: String): Int = options(language, mode).size
}

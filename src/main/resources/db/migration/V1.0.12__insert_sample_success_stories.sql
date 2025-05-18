-- First, insert sample users
INSERT INTO users (id, email, password, first_name, last_name, user_type, created_at, updated_at) VALUES 
(1001, 'ayse@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'Ayşe', 'Yılmaz', 'ENTREPRENEUR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1002, 'mehmet@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'Mehmet', 'Kaya', 'ENTREPRENEUR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1003, 'zeynep@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'Zeynep', 'Demir', 'ENTREPRENEUR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1004, 'ali@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'Ali', 'Yıldız', 'ENTREPRENEUR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1005, 'fatma@example.com', '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', 'Fatma', 'Şahin', 'ENTREPRENEUR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample success stories
INSERT INTO success_stories (
    title,
    content,
    story,
    image_url,
    category,
    approved,
    is_approved,
    support_count,
    author_id,
    created_at,
    updated_at
) VALUES 
(
    'El Yapımı Sabun İşimden Global Markaya',
    'Merhaba, ben Ayşe. 5 yıl önce evde el yapımı sabun üretmeye başladım. İlk başta sadece arkadaşlarıma ve aileme satıyordum. Sosyal medyada paylaştığım ürünlerim kısa sürede ilgi görmeye başladı. Şimdi kendi mağazam ve online satış platformum var. Yıllık cirom 1 milyon TL''yi aştı ve 5 kişilik bir ekibim var. Başarılı olmak için en önemli şey, ürününüzün kalitesine inanmak ve müşteri memnuniyetini her şeyin üstünde tutmaktır.',
    'Merhaba, ben Ayşe. 5 yıl önce evde el yapımı sabun üretmeye başladım. İlk başta sadece arkadaşlarıma ve aileme satıyordum. Sosyal medyada paylaştığım ürünlerim kısa sürede ilgi görmeye başladı. Şimdi kendi mağazam ve online satış platformum var. Yıllık cirom 1 milyon TL''yi aştı ve 5 kişilik bir ekibim var. Başarılı olmak için en önemli şey, ürününüzün kalitesine inanmak ve müşteri memnuniyetini her şeyin üstünde tutmaktır.',
    'https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b',
    'El Yapımı Ürünler',
    true,
    true,
    156,
    1001,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'Köy Yumurtasından Organik Gıda İmparatorluğuna',
    'Ben Mehmet, 3 yıl önce köydeki ailemizin küçük yumurta üretimini profesyonel bir işletmeye dönüştürdüm. Organik sertifikalarımızı aldık, modern üretim tesisimizi kurduk. Şu anda 10 ilde 50''den fazla markete tedarik sağlıyoruz. Geçen yıl 2 milyon TL ciro yaptık. Başarı hikayemizin sırrı, kaliteden ödün vermemek ve sürdürülebilir üretim prensiplerine bağlı kalmaktır.',
    'Ben Mehmet, 3 yıl önce köydeki ailemizin küçük yumurta üretimini profesyonel bir işletmeye dönüştürdüm. Organik sertifikalarımızı aldık, modern üretim tesisimizi kurduk. Şu anda 10 ilde 50''den fazla markete tedarik sağlıyoruz. Geçen yıl 2 milyon TL ciro yaptık. Başarı hikayemizin sırrı, kaliteden ödün vermemek ve sürdürülebilir üretim prensiplerine bağlı kalmaktır.',
    'https://images.unsplash.com/photo-1582722872445-44dc5f7e2e8b',
    'Organik Gıda',
    true,
    true,
    243,
    1002,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'Tekstil Atıklarından Moda Markasına',
    'Merhaba, ben Zeynep. Moda tasarımı eğitimim sonrası sürdürülebilir moda konusunda çalışmaya başladım. Tekstil atıklarını değerlendirerek yeni ürünler tasarlıyorum. İlk koleksiyonum büyük ilgi gördü ve şimdi kendi mağazalarım var. Yıllık cirom 3 milyon TL''yi aştı. Başarılı olmak için yaratıcılık ve sürdürülebilirlik ilkelerini birleştirmek çok önemli.',
    'Merhaba, ben Zeynep. Moda tasarımı eğitimim sonrası sürdürülebilir moda konusunda çalışmaya başladım. Tekstil atıklarını değerlendirerek yeni ürünler tasarlıyorum. İlk koleksiyonum büyük ilgi gördü ve şimdi kendi mağazalarım var. Yıllık cirom 3 milyon TL''yi aştı. Başarılı olmak için yaratıcılık ve sürdürülebilirlik ilkelerini birleştirmek çok önemli.',
    'https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5',
    'Sürdürülebilir Moda',
    true,
    true,
    189,
    1003,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'Yazılım Geliştiriciden Tech Startup''a',
    'Ben Ali, 4 yıl önce bir yazılım şirketi kurduk. İlk başta küçük projelerle başladık, şimdi 20 kişilik bir ekibimiz var. Geçen yıl 5 milyon TL ciro yaptık ve yurtdışına açıldık. Başarı hikayemizin anahtarı, sürekli öğrenmek ve teknolojiyi yakından takip etmektir.',
    'Ben Ali, 4 yıl önce bir yazılım şirketi kurduk. İlk başta küçük projelerle başladık, şimdi 20 kişilik bir ekibimiz var. Geçen yıl 5 milyon TL ciro yaptık ve yurtdışına açıldık. Başarı hikayemizin anahtarı, sürekli öğrenmek ve teknolojiyi yakından takip etmektir.',
    'https://images.unsplash.com/photo-1552664730-d307ca884978',
    'Teknoloji',
    true,
    true,
    312,
    1004,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'Ev Yemeklerinden Restoran Zincirine',
    'Merhaba, ben Fatma. Ev yemekleri yaparak başladığım yolculuğum, şimdi 3 şubeli bir restoran zincirine dönüştü. Geleneksel lezzetleri modern sunumlarla birleştirerek fark yarattık. Yıllık ciromuz 4 milyon TL''yi aştı. Başarılı olmak için en önemli şey, kaliteden ödün vermemek ve müşteri memnuniyetini her şeyin üstünde tutmaktır.',
    'Merhaba, ben Fatma. Ev yemekleri yaparak başladığım yolculuğum, şimdi 3 şubeli bir restoran zincirine dönüştü. Geleneksel lezzetleri modern sunumlarla birleştirerek fark yarattık. Yıllık ciromuz 4 milyon TL''yi aştı. Başarılı olmak için en önemli şey, kaliteden ödün vermemek ve müşteri memnuniyetini her şeyin üstünde tutmaktır.',
    'https://images.unsplash.com/photo-1555396273-367ea4eb4db5',
    'Gastronomi',
    true,
    true,
    278,
    1005,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insert sample comments for the first story
INSERT INTO story_comments (
    content,
    story_id,
    author_id,
    created_at,
    updated_at
) VALUES 
(
    'Harika bir başarı hikayesi! Ben de benzer bir yolculuğa çıkmak istiyorum. Tavsiyeleriniz için teşekkürler.',
    1,
    1002,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'Ürünlerinizi kullanıyorum, gerçekten çok kaliteli. Başarılarınızın devamını dilerim.',
    1,
    1003,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'Bu hikaye bana ilham verdi. Ben de kendi işimi kurmak istiyorum.',
    1,
    1004,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
); 
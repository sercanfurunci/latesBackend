-- Insert sample educational content
INSERT INTO sc_seniorproject.educational_contents (
    title,
    description,
    category,
    image_url,
    duration,
    video_url,
    estimated_duration,
    difficulty,
    is_published,
    created_at,
    updated_at
) VALUES 
(
    'Girişimcilik 101: Başlangıç Rehberi',
    'Bu eğitimde, girişimcilik dünyasına ilk adımlarınızı atarken bilmeniz gereken temel kavramları ve stratejileri öğreneceksiniz. İş planı oluşturma, pazar araştırması ve finansal planlama konularında pratik bilgiler sunulmaktadır.',
    'Girişimcilik',
    'https://images.unsplash.com/photo-1507679799987-c73779587ccf',
    45,
    'https://example.com/videos/girisimcilik-101',
    60,
    'Başlangıç',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'Etkili Pazarlama Stratejileri',
    'Modern pazarlama dünyasında başarılı olmak için gerekli stratejileri ve teknikleri öğrenin. Sosyal medya pazarlaması, içerik pazarlaması ve e-posta pazarlaması konularında detaylı bilgiler içerir.',
    'Pazarlama',
    'https://images.unsplash.com/photo-1557838923-2985c318be48',
    60,
    'https://example.com/videos/pazarlama-stratejileri',
    90,
    'Orta',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'Ürün Sunumu Teknikleri',
    'Ürünlerinizi en etkili şekilde nasıl sunacağınızı öğrenin. Sunum teknikleri, müşteri ikna yöntemleri ve ürün özelliklerini vurgulama stratejileri bu eğitimde detaylı olarak ele alınmaktadır.',
    'Ürün Sunumu',
    'https://images.unsplash.com/photo-1552664730-d307ca884978',
    30,
    'https://example.com/videos/urun-sunumu',
    45,
    'Başlangıç',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'Satış Teknikleri ve Müzakere',
    'Profesyonel satış teknikleri ve müzakere becerilerinizi geliştirin. Müşteri ilişkileri yönetimi, satış süreçleri ve başarılı müzakereler için gerekli stratejiler bu eğitimde bulunmaktadır.',
    'Satış Stratejisi',
    'https://images.unsplash.com/photo-1554224155-6726b3ff858f',
    75,
    'https://example.com/videos/satis-teknikleri',
    120,
    'İleri',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'Müşteri Hizmetleri Mükemmelliği',
    'Müşteri memnuniyetini artırmak için gerekli becerileri ve stratejileri öğrenin. Müşteri iletişimi, sorun çözme ve hizmet kalitesi konularında pratik bilgiler sunulmaktadır.',
    'Müşteri Hizmetleri',
    'https://images.unsplash.com/photo-1552581234-26160f608093',
    45,
    'https://example.com/videos/musteri-hizmetleri',
    60,
    'Orta',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insert sample resources for the first content
INSERT INTO sc_seniorproject.educational_content_resources (educational_content_id, resources)
VALUES 
(1, 'https://example.com/resources/girisimcilik-kitabi.pdf'),
(1, 'https://example.com/resources/is-plani-sablonu.docx'),
(1, 'https://example.com/resources/pazar-arastirma-rehberi.pdf');

-- Insert sample video URLs for the first content
INSERT INTO sc_seniorproject.educational_content_video_urls (educational_content_id, video_urls)
VALUES 
(1, 'https://example.com/videos/girisimcilik-101-bolum1'),
(1, 'https://example.com/videos/girisimcilik-101-bolum2'),
(1, 'https://example.com/videos/girisimcilik-101-bolum3'); 
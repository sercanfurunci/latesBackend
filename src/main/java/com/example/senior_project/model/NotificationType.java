package com.example.senior_project.model;

public enum NotificationType {
    // Teklif bildirimleri
    NEW_OFFER("Yeni Teklif"),
    OFFER_ACCEPTED("Teklif Kabul Edildi"),
    OFFER_REJECTED("Teklif Reddedildi"),
    OFFER_CANCELLED("Teklif İptal Edildi"),
    OFFER_STATUS_CHANGE("Teklif Durumu Değişti"),

    // Sipariş bildirimleri
    ORDER_CREATED("Yeni Sipariş"),
    ORDER_SHIPPED("Sipariş Kargoya Verildi"),
    ORDER_DELIVERED("Sipariş Teslim Edildi"),
    ORDER_CANCELLED("Sipariş İptal Edildi"),
    ORDER_STATUS_CHANGE("Sipariş Durumu Değişti"),

    // Ürün bildirimleri
    PRODUCT_CREATED("Yeni Ürün Eklendi"),
    PRODUCT_UPDATED("Ürün Güncellendi"),
    PRODUCT_DELETED("Ürün Silindi"),
    PRODUCT_APPROVED("Ürün Onaylandı"),
    PRODUCT_REJECTED("Ürün Reddedildi"),
    PRODUCT_STATUS_CHANGE("Ürün Durumu Değişti"),
    PRODUCT_STOCK_LOW("Ürün Stok Azaldı"),
    PRODUCT_OUT_OF_STOCK("Ürün Stokta Yok"),

    // Kullanıcı bildirimleri
    USER_REGISTERED("Yeni Kayıt"),
    USER_VERIFIED("Hesap Doğrulandı"),
    USER_BLOCKED("Hesap Engellendi"),
    SELLER_VERIFIED("Satıcı Onaylandı"),
    SELLER_REJECTED("Satıcı Reddedildi"),

    // Yorum bildirimleri
    COMMENT_RECEIVED("Yeni Yorum"),
    COMMENT_REPLIED("Yoruma Yanıt"),
    COMMENT_REPORTED("Yorum Şikayet Edildi"),

    // Mesaj bildirimleri
    NEW_MESSAGE("Yeni Mesaj"),
    MESSAGE_READ("Mesaj Okundu"),

    // Sistem bildirimleri
    SYSTEM_MESSAGE("Sistem Mesajı"),
    MAINTENANCE("Bakım Bildirimi"),
    ERROR("Hata Bildirimi"),

    // Yeni eklenen tip
    OFFER_RECEIVED("Teklif Alındı"),
    PAYMENT_RECEIVED("Ödeme Alındı"),
    PAYMENT_FAILED("Ödeme Başarısız");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

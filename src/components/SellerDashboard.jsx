import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const SellerDashboard = () => {
  const [newProduct, setNewProduct] = useState({
    title: "",
    description: "",
    price: "",
    stock: "",
    categoryId: "",
    images: [],
  });
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleNewProductSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    // Açıklama kontrolü
    if (newProduct.description.length < 10) {
      setError("Ürün açıklaması en az 10 karakter olmalıdır!");
      return;
    }

    // Token ve yetki kontrolü
    const token = localStorage.getItem("token");
    const userType = localStorage.getItem("userType");

    if (!token || userType !== "SELLER") {
      setError(
        "Oturum süreniz dolmuş veya yetkiniz yok. Lütfen tekrar giriş yapın."
      );
      localStorage.clear();
      navigate("/login");
      return;
    }

    try {
      // Önce ürün verilerini gönder
      const productData = {
        title: newProduct.title.trim(),
        description: newProduct.description.trim(),
        price: parseFloat(newProduct.price),
        stock: parseInt(newProduct.stock),
        categoryId: parseInt(newProduct.categoryId),
        type: "FOOD",
        images: [],
        tags: [],
        ingredients: "",
        preparationTime: "",
      };

      const response = await axios.post(
        "http://localhost:8080/api/v1/seller/products",
        productData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      // Eğer resimler varsa, ayrı bir istekle resimleri yükle
      if (response.data && newProduct.images.length > 0) {
        const productId = response.data.id;
        const imageFormData = new FormData();

        // Her bir resmi FormData'ya ekle
        newProduct.images.forEach((image) => {
          imageFormData.append("images", image);
        });

        try {
          const token = localStorage.getItem("token");
          if (!token) {
            throw new Error("Oturum bilgisi bulunamadı");
          }

          console.log("Resim yükleme isteği gönderiliyor...");
          console.log("Ürün ID:", productId);
          console.log("Resim sayısı:", newProduct.images.length);
          console.log("Token:", token.substring(0, 20) + "...");

          const uploadResponse = await axios.post(
            `http://localhost:8080/api/v1/seller/products/${productId}/images`,
            imageFormData,
            {
              headers: {
                Authorization: `Bearer ${token}`,
              },
              withCredentials: true,
            }
          );

          console.log("Resim yükleme başarılı:", uploadResponse.data);
        } catch (imageError) {
          console.error("Resim yükleme hatası:", imageError);
          console.error("Hata detayları:", {
            message: imageError.message,
            response: imageError.response?.data,
            status: imageError.response?.status,
            headers: imageError.response?.headers,
          });

          setError(
            "Ürün oluşturuldu fakat resimler yüklenirken bir hata oluştu: " +
              (imageError.response?.data || imageError.message)
          );
        }
      }

      // Başarılı ekleme sonrası
      await fetchProducts(); // Ürün listesini yenile
      setShowAddProductModal(false); // Modalı kapat
      setNewProduct({
        // Form verilerini sıfırla
        title: "",
        description: "",
        price: "",
        stock: "",
        categoryId: "",
        images: [],
      });
    } catch (error) {
      console.error("Hata detayları:", {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status,
      });

      if (error.response?.status === 403) {
        setError(
          "Bu işlem için yetkiniz bulunmuyor. Lütfen tekrar giriş yapın."
        );
        localStorage.clear();
        navigate("/login");
      } else if (error.response?.status === 401) {
        setError("Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.");
        navigate("/login");
      } else {
        setError(
          error.response?.data?.message || "Ürün eklenirken bir hata oluştu."
        );
      }
    }
  };

  return <div>{/* Formun kalanı burada kalacak */}</div>;
};

export default SellerDashboard;

import React, { useState, useEffect } from "react";
import api from "../utils/axios";

const SellerDashboard = () => {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [error, setError] = useState("");
  const [showNewProductModal, setShowNewProductModal] = useState(false);
  const [newProduct, setNewProduct] = useState({
    title: "",
    description: "",
    price: "",
    categoryId: "",
    stock: "",
    images: [],
  });
  const [sortBy, setSortBy] = useState("newest");
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    fetchProducts();
    fetchCategories();
  }, [sortBy]);

  const handleNewProductSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        setError("Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.");
        return;
      }

      const userType = localStorage.getItem("userType");
      if (userType !== "SELLER") {
        setError("Bu sayfaya erişim yetkiniz yok.");
        return;
      }

      // Ürün verilerini gönder
      const productData = {
        title: newProduct.title,
        description: newProduct.description,
        price: newProduct.price,
        categoryId: newProduct.categoryId,
        stock: newProduct.stock,
      };

      const response = await api.post("/api/v1/seller/products", productData);
      const createdProduct = response.data;

      // Eğer resimler varsa, ayrı bir istek ile yükle
      if (newProduct.images.length > 0) {
        const formData = new FormData();
        newProduct.images.forEach((image) => {
          formData.append("images", image);
        });

        await api.post(
          `/api/v1/seller/products/${createdProduct.id}/images`,
          formData,
          {
            headers: {
              "Content-Type": "multipart/form-data",
            },
          }
        );
      }

      setShowNewProductModal(false);
      setNewProduct({
        title: "",
        description: "",
        price: "",
        categoryId: "",
        stock: "",
        images: [],
      });
      setSuccessMessage("Ürün başarıyla eklendi!");
      fetchProducts();
    } catch (error) {
      console.error("Ürün ekleme hatası:", error);
      setError(error.response?.data || "Ürün eklenirken bir hata oluştu");
    } finally {
      setLoading(false);
    }
  };

  const handleEditProduct = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const response = await api.put(
        `/api/v1/seller/products/${selectedProduct.id}`,
        {
          title: selectedProduct.title,
          description: selectedProduct.description,
          price: selectedProduct.price,
          categoryId: selectedProduct.categoryId,
          stock: selectedProduct.stock,
        }
      );

      setShowEditModal(false);
      setSelectedProduct(null);
      setSuccessMessage("Ürün başarıyla güncellendi!");
      fetchProducts();
    } catch (error) {
      console.error("Ürün güncelleme hatası:", error);
      setError(error.response?.data || "Ürün güncellenirken bir hata oluştu");
    } finally {
      setLoading(false);
    }
  };

  const fetchProducts = async () => {
    try {
      const response = await api.get("/api/v1/seller/products", {
        params: { sortBy },
      });
      setProducts(response.data);
    } catch (error) {
      console.error("Ürünler yüklenirken hata:", error);
      setError("Ürünler yüklenirken bir hata oluştu");
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await api.get("/api/v1/categories");
      setCategories(response.data);
    } catch (error) {
      console.error("Kategoriler yüklenirken hata:", error);
      setError("Kategoriler yüklenirken bir hata oluştu");
    }
  };

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);
    setNewProduct((prev) => ({
      ...prev,
      images: [...prev.images, ...files],
    }));
  };

  const handleDeleteProduct = async (productId) => {
    if (!window.confirm("Bu ürünü silmek istediğinizden emin misiniz?")) {
      return;
    }

    try {
      await api.delete(`/api/v1/seller/products/${productId}`);
      setSuccessMessage("Ürün başarıyla silindi!");
      fetchProducts();
    } catch (error) {
      console.error("Ürün silme hatası:", error);
      setError("Ürün silinirken bir hata oluştu");
    }
  };

  const filteredProducts = products.filter((product) =>
    product.title.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Satıcı Paneli</h1>
        <button
          onClick={() => setShowNewProductModal(true)}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          Yeni Ürün Ekle
        </button>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {successMessage && (
        <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4">
          {successMessage}
        </div>
      )}

      <div className="mb-6 flex gap-4">
        <input
          type="text"
          placeholder="Ürün ara..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="border p-2 rounded flex-grow"
        />
        <select
          value={sortBy}
          onChange={(e) => setSortBy(e.target.value)}
          className="border p-2 rounded"
        >
          <option value="newest">En Yeni</option>
          <option value="price-asc">Fiyat (Düşükten Yükseğe)</option>
          <option value="price-desc">Fiyat (Yüksekten Düşüğe)</option>
        </select>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredProducts.map((product) => (
          <div
            key={product.id}
            className="border rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow"
          >
            <img
              src={
                product.images[0]
                  ? `http://localhost:8080${product.images[0]}`
                  : "/placeholder.png"
              }
              alt={product.title}
              className="w-full h-48 object-cover rounded mb-4"
            />
            <h3 className="text-lg font-semibold mb-2">{product.title}</h3>
            <p className="text-gray-600 mb-2">{product.description}</p>
            <p className="text-blue-600 font-bold mb-2">{product.price} TL</p>
            <p className="text-gray-500 mb-4">Stok: {product.stock}</p>
            <div className="flex justify-between">
              <button
                onClick={() => handleDeleteProduct(product.id)}
                className="text-red-500 hover:text-red-700"
              >
                Sil
              </button>
              <button
                onClick={() => {
                  setSelectedProduct(product);
                  setShowEditModal(true);
                }}
                className="text-blue-500 hover:text-blue-700"
              >
                Düzenle
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Yeni Ürün Modal */}
      {showNewProductModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white p-6 rounded-lg w-full max-w-md">
            <h2 className="text-xl font-bold mb-4">Yeni Ürün Ekle</h2>
            <form onSubmit={handleNewProductSubmit}>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Ürün Adı</label>
                <input
                  type="text"
                  value={newProduct.title}
                  onChange={(e) =>
                    setNewProduct({ ...newProduct, title: e.target.value })
                  }
                  className="w-full border p-2 rounded"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Açıklama</label>
                <textarea
                  value={newProduct.description}
                  onChange={(e) =>
                    setNewProduct({
                      ...newProduct,
                      description: e.target.value,
                    })
                  }
                  className="w-full border p-2 rounded"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Fiyat</label>
                <input
                  type="number"
                  value={newProduct.price}
                  onChange={(e) =>
                    setNewProduct({ ...newProduct, price: e.target.value })
                  }
                  className="w-full border p-2 rounded"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Kategori</label>
                <select
                  value={newProduct.categoryId}
                  onChange={(e) =>
                    setNewProduct({ ...newProduct, categoryId: e.target.value })
                  }
                  className="w-full border p-2 rounded"
                  required
                >
                  <option value="">Kategori Seçin</option>
                  {categories.map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Stok</label>
                <input
                  type="number"
                  value={newProduct.stock}
                  onChange={(e) =>
                    setNewProduct({ ...newProduct, stock: e.target.value })
                  }
                  className="w-full border p-2 rounded"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Resimler</label>
                <input
                  type="file"
                  multiple
                  accept="image/*"
                  onChange={handleImageChange}
                  className="w-full border p-2 rounded"
                />
                <div className="mt-2 flex flex-wrap gap-2">
                  {newProduct.images.map((image, index) => (
                    <div key={index} className="relative">
                      <img
                        src={URL.createObjectURL(image)}
                        alt={`Preview ${index + 1}`}
                        className="w-20 h-20 object-cover rounded"
                      />
                      <button
                        type="button"
                        onClick={() => {
                          setNewProduct((prev) => ({
                            ...prev,
                            images: prev.images.filter((_, i) => i !== index),
                          }));
                        }}
                        className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center"
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              </div>
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowNewProductModal(false)}
                  className="px-4 py-2 border rounded hover:bg-gray-100"
                >
                  İptal
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50"
                >
                  {loading ? "Ekleniyor..." : "Ekle"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Düzenleme Modal */}
      {showEditModal && selectedProduct && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white p-6 rounded-lg w-full max-w-md">
            <h2 className="text-xl font-bold mb-4">Ürün Düzenle</h2>
            <form onSubmit={handleEditProduct}>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Ürün Adı</label>
                <input
                  type="text"
                  value={selectedProduct.title}
                  onChange={(e) =>
                    setSelectedProduct({
                      ...selectedProduct,
                      title: e.target.value,
                    })
                  }
                  className="w-full border p-2 rounded"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Açıklama</label>
                <textarea
                  value={selectedProduct.description}
                  onChange={(e) =>
                    setSelectedProduct({
                      ...selectedProduct,
                      description: e.target.value,
                    })
                  }
                  className="w-full border p-2 rounded"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Fiyat</label>
                <input
                  type="number"
                  value={selectedProduct.price}
                  onChange={(e) =>
                    setSelectedProduct({
                      ...selectedProduct,
                      price: e.target.value,
                    })
                  }
                  className="w-full border p-2 rounded"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Kategori</label>
                <select
                  value={selectedProduct.categoryId}
                  onChange={(e) =>
                    setSelectedProduct({
                      ...selectedProduct,
                      categoryId: e.target.value,
                    })
                  }
                  className="w-full border p-2 rounded"
                  required
                >
                  <option value="">Kategori Seçin</option>
                  {categories.map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Stok</label>
                <input
                  type="number"
                  value={selectedProduct.stock}
                  onChange={(e) =>
                    setSelectedProduct({
                      ...selectedProduct,
                      stock: e.target.value,
                    })
                  }
                  className="w-full border p-2 rounded"
                  required
                />
              </div>
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => {
                    setShowEditModal(false);
                    setSelectedProduct(null);
                  }}
                  className="px-4 py-2 border rounded hover:bg-gray-100"
                >
                  İptal
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:opacity-50"
                >
                  {loading ? "Güncelleniyor..." : "Güncelle"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default SellerDashboard;

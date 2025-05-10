import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  FaHeart,
  FaRegHeart,
  FaUserPlus,
  FaUserCheck,
  FaShoppingCart,
} from "react-icons/fa";
import axios from "axios";
import "./ProductList.css";

// API instance
const api = axios.create({
  baseURL: "http://localhost:8080/api/v1",
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

function ProductList() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [categories, setCategories] = useState([]);
  const [favorites, setFavorites] = useState([]);
  const [followedSellers, setFollowedSellers] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("all");
  const [sortBy, setSortBy] = useState("default");
  const [priceRange, setPriceRange] = useState({ min: "", max: "" });
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userRole, setUserRole] = useState(null);
  const navigate = useNavigate();

  // Kullanıcı durumunu kontrol et
  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      setIsLoggedIn(true);
      const role = localStorage.getItem("userRole");
      setUserRole(role);
    }
  }, []);

  // Verileri yükle
  useEffect(() => {
    const fetchData = async () => {
      try {
        const [productsRes, categoriesRes] = await Promise.all([
          api.get("/products"),
          api.get("/categories"),
        ]);

        console.log("Products data:", productsRes.data.content);
        setProducts(productsRes.data.content);
        setCategories(categoriesRes.data);

        if (isLoggedIn && userRole === "BUYER") {
          try {
            const [favoritesRes, followingRes] = await Promise.all([
              api.get("/buyer/favorites"),
              api.get("/buyer/following"),
            ]);
            setFavorites(favoritesRes.data.map((f) => f.id));
            setFollowedSellers(followingRes.data.map((s) => s.id));
          } catch (error) {
            console.error("Favori ve takip bilgileri alınamadı:", error);
          }
        }
      } catch (err) {
        console.error("Veri yükleme hatası:", err);
        setError("Veriler yüklenirken bir hata oluştu.");
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isLoggedIn, userRole]);

  // Favori işlemleri
  const handleToggleFavorite = async (productId, event) => {
    event.preventDefault();
    event.stopPropagation();

    if (!isLoggedIn) {
      navigate("/login");
      return;
    }

    try {
      const response = await api.post(
        `/buyer/favorites/toggle/${productId}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      if (response.status === 200) {
        setFavorites((prev) =>
          prev.includes(productId)
            ? prev.filter((id) => id !== productId)
            : [...prev, productId]
        );
      }
    } catch (error) {
      console.error("Favori işlemi başarısız:", error);
      if (error.response?.status === 403) {
        alert(
          "Bu işlemi gerçekleştirmek için alıcı hesabına sahip olmanız gerekmektedir."
        );
      }
    }
  };

  // Takip işlemleri
  const handleToggleFollow = async (sellerId, event) => {
    event.stopPropagation();

    if (!isLoggedIn) {
      navigate("/login");
      return;
    }

    try {
      if (followedSellers.includes(sellerId)) {
        await api.delete(`/buyer/unfollow/${sellerId}`);
        setFollowedSellers((prev) => prev.filter((id) => id !== sellerId));
      } else {
        await api.post(`/buyer/follow/${sellerId}`);
        setFollowedSellers((prev) => [...prev, sellerId]);
      }
    } catch (error) {
      console.error("Takip işlemi başarısız:", error);
      if (error.response?.status === 401) {
        navigate("/login");
      } else {
        alert("Takip işlemi gerçekleştirilemedi.");
      }
    }
  };

  // Sepete ekleme işlemi
  const handleAddToCart = async (productId, event) => {
    event.stopPropagation();

    if (!isLoggedIn) {
      navigate("/login");
      return;
    }

    try {
      await api.post("/cart/add", { productId, quantity: 1 });
      alert("Ürün sepete eklendi!");
    } catch (error) {
      console.error("Sepete ekleme hatası:", error);
      if (error.response?.status === 401) {
        navigate("/login");
      } else {
        alert("Ürün sepete eklenemedi.");
      }
    }
  };

  // Filtreleme ve sıralama
  const filteredAndSortedProducts = () => {
    let filtered = [...products];

    // Kategori filtresi
    if (selectedCategory !== "all") {
      filtered = filtered.filter(
        (product) => product.category.id.toString() === selectedCategory
      );
    }

    // Arama filtresi
    if (searchTerm) {
      const searchLower = searchTerm.toLowerCase();
      filtered = filtered.filter(
        (product) =>
          product.title.toLowerCase().includes(searchLower) ||
          product.description?.toLowerCase().includes(searchLower) ||
          product.category?.name.toLowerCase().includes(searchLower)
      );
    }

    // Fiyat filtresi
    if (priceRange.min !== "") {
      filtered = filtered.filter(
        (product) => product.price >= parseFloat(priceRange.min)
      );
    }
    if (priceRange.max !== "") {
      filtered = filtered.filter(
        (product) => product.price <= parseFloat(priceRange.max)
      );
    }

    // Sıralama
    switch (sortBy) {
      case "price-asc":
        filtered.sort((a, b) => a.price - b.price);
        break;
      case "price-desc":
        filtered.sort((a, b) => b.price - a.price);
        break;
      case "name-asc":
        filtered.sort((a, b) => a.title.localeCompare(b.title));
        break;
      case "name-desc":
        filtered.sort((a, b) => b.title.localeCompare(a.title));
        break;
      default:
        break;
    }

    return filtered;
  };

  const handlePriceRangeChange = (e) => {
    const { name, value } = e.target;
    setPriceRange((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const resetFilters = () => {
    setSearchTerm("");
    setSelectedCategory("all");
    setSortBy("default");
    setPriceRange({ min: "", max: "" });
  };

  if (loading) {
    return (
      <div className="product-list-loading">
        <div className="product-list-spinner"></div>
      </div>
    );
  }

  if (error) {
    return <div className="product-list-error">{error}</div>;
  }

  const filteredProducts = filteredAndSortedProducts();

  return (
    <div className="product-list-container">
      {/* Filtreler */}
      <div className="product-list-filters">
        <div className="product-list-search">
          <input
            type="text"
            placeholder="Ürün Ara..."
            className="product-list-search-input"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="product-list-filter-options">
          <select
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            className="product-list-category-select"
          >
            <option value="all">Tüm Kategoriler</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>

          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="product-list-sort-select"
          >
            <option value="default">Varsayılan Sıralama</option>
            <option value="price-asc">Fiyat (Düşükten Yükseğe)</option>
            <option value="price-desc">Fiyat (Yüksekten Düşüğe)</option>
            <option value="name-asc">İsim (A-Z)</option>
            <option value="name-desc">İsim (Z-A)</option>
          </select>

          <div className="product-list-price-range">
            <input
              type="number"
              name="min"
              placeholder="Min Fiyat"
              value={priceRange.min}
              onChange={handlePriceRangeChange}
              className="product-list-price-input"
              min="0"
            />
            <input
              type="number"
              name="max"
              placeholder="Max Fiyat"
              value={priceRange.max}
              onChange={handlePriceRangeChange}
              className="product-list-price-input"
              min="0"
            />
          </div>

          <button onClick={resetFilters} className="product-list-reset-filters">
            Filtreleri Sıfırla
          </button>
        </div>
      </div>

      {/* Ürün Listesi */}
      <h2 className="product-list-title">
        {filteredProducts.length > 0
          ? `${filteredProducts.length} Ürün Bulundu`
          : "Ürün Bulunamadı"}
      </h2>

      {filteredProducts.length === 0 ? (
        <div className="product-list-no-results">
          Arama kriterlerinize uygun ürün bulunamadı.
        </div>
      ) : (
        <div className="product-list-grid">
          {filteredProducts.map((product) => (
            <div key={product.id} className="product-list-card">
              <div className="product-list-image">
                <img
                  src={
                    product.images && product.images.length > 0
                      ? `http://localhost:8080/uploads/products/${
                          product.id
                        }/${product.images[0].split("/").pop()}`
                      : "/default-product.png"
                  }
                  alt={product.title}
                  onClick={() => navigate(`/products/${product.id}`)}
                />
                {isLoggedIn && userRole === "BUYER" && (
                  <button
                    className={`product-list-favorite-button ${
                      favorites.includes(product.id) ? "favorited" : ""
                    }`}
                    onClick={(e) => handleToggleFavorite(product.id, e)}
                    aria-label={
                      favorites.includes(product.id)
                        ? "Favorilerden çıkar"
                        : "Favorilere ekle"
                    }
                  >
                    {favorites.includes(product.id) ? (
                      <FaHeart />
                    ) : (
                      <FaRegHeart />
                    )}
                  </button>
                )}
              </div>
              <div className="product-list-details">
                <h4 onClick={() => navigate(`/products/${product.id}`)}>
                  {product.title}
                </h4>
                <p className="product-list-description">
                  {product.description?.substring(0, 100)}
                  {product.description?.length > 100 ? "..." : ""}
                </p>
                <p className="product-list-category">
                  {product.category?.name}
                </p>
                <p className="product-list-price">{product.price} TL</p>

                <div className="product-list-seller-info">
                  <img
                    src={product.seller.profilePicture || "/default-avatar.png"}
                    alt={product.seller.firstName}
                    className="product-list-seller-avatar"
                    onClick={() => navigate(`/seller/${product.seller.id}`)}
                  />
                  <div className="product-list-seller-details">
                    <span
                      className="product-list-seller-name"
                      onClick={() => navigate(`/seller/${product.seller.id}`)}
                    >
                      {product.seller.firstName} {product.seller.lastName}
                    </span>
                    {isLoggedIn && userRole === "BUYER" && (
                      <button
                        className={`product-list-follow-button ${
                          followedSellers.includes(product.seller.id)
                            ? "following"
                            : ""
                        }`}
                        onClick={(e) =>
                          handleToggleFollow(product.seller.id, e)
                        }
                      >
                        {followedSellers.includes(product.seller.id) ? (
                          <>
                            <FaUserCheck /> Takipte
                          </>
                        ) : (
                          <>
                            <FaUserPlus /> Takip Et
                          </>
                        )}
                      </button>
                    )}
                  </div>
                </div>

                <button
                  className="product-list-add-to-cart"
                  onClick={(e) => handleAddToCart(product.id, e)}
                >
                  <FaShoppingCart /> Sepete Ekle
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default ProductList;

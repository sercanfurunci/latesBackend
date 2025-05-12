import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import { FaUserCheck, FaUserPlus } from "react-icons/fa";
import "./SellerProfile.css";

// API instance oluştur
const api = axios.create({
  baseURL: "http://localhost:8080/api/v1",
});

// Request interceptor ekle
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

function SellerProfile() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [seller, setSeller] = useState(null);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isFollowing, setIsFollowing] = useState(false);
  const [userRole, setUserRole] = useState(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem("token");
    const user = JSON.parse(localStorage.getItem("user"));

    if (token && user) {
      try {
        const tokenPayload = JSON.parse(atob(token.split(".")[1]));
        const userRole = tokenPayload.role.replace("ROLE_", "");
        
        if (userRole === user.userType) {
          setIsLoggedIn(true);
          setUserRole(user.userType);
        } else {
          console.error("Token rolü ile kullanıcı rolü uyuşmuyor");
          setIsLoggedIn(false);
          setUserRole(null);
          localStorage.removeItem("token");
          localStorage.removeItem("user");
          navigate("/login");
        }
      } catch (error) {
        console.error("Token decode hatası:", error);
        setIsLoggedIn(false);
        setUserRole(null);
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        navigate("/login");
      }
    }
  }, [navigate]);

  useEffect(() => {
    const fetchSellerData = async () => {
      try {
        setLoading(true);
        setError(null);

        // Satıcı bilgilerini al
        const sellerRes = await api.get(`/users/${id}`);
        if (!sellerRes.data) {
          throw new Error("Satıcı bilgileri bulunamadı");
        }
        const sellerData = sellerRes.data;

        // Ürün bilgilerini al
        const productsRes = await api.get(`/products?sellerId=${id}`);
        const productsData = productsRes.data.content || [];
        const filteredProducts = productsData.filter(
          (product) => product.seller.id === parseInt(id)
        );

        setSeller(sellerData);
        setProducts(filteredProducts);

        // Takip durumunu kontrol et
        if (isLoggedIn && userRole === "BUYER") {
          try {
            const followingRes = await api.get("/buyer/following");
            const isFollowing = followingRes.data.some(
              (f) => f.id === parseInt(id)
            );
            setIsFollowing(isFollowing);
          } catch (error) {
            console.error("Takip durumu kontrol edilemedi:", error);
            setIsFollowing(false);
          }
        }
      } catch (err) {
        console.error("Veri yükleme hatası:", err);
        if (err.response?.status === 404) {
          setError("Satıcı bulunamadı.");
        } else if (err.response?.status === 403) {
          setError("Bu sayfaya erişim izniniz yok.");
        } else {
          setError(
            "Veriler yüklenirken bir hata oluştu. Lütfen daha sonra tekrar deneyin."
          );
        }
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchSellerData();
    }
  }, [id, isLoggedIn, userRole]);

  const handleToggleFollow = async () => {
    if (!isLoggedIn) {
      navigate("/login");
      return;
    }

    try {
      const token = localStorage.getItem("token");
      if (!token) {
        throw new Error("Token bulunamadı");
      }

      // Token'ı decode et ve kontrol et
      const tokenPayload = JSON.parse(atob(token.split(".")[1]));
      if (!tokenPayload.authorities?.includes("ROLE_BUYER")) {
        throw new Error("Bu işlem için BUYER yetkisi gerekiyor");
      }

      if (isFollowing) {
        console.log("Unfollow işlemi başlatılıyor...");
        await api.delete(`/buyer/unfollow/${id}`);
        setIsFollowing(false);
      } else {
        console.log("Follow işlemi başlatılıyor...");
        await api.post(`/buyer/follow/${id}`);
        setIsFollowing(true);
      }

      // Satıcı bilgilerini güncelle
      const updatedSellerRes = await api.get(`/users/${id}`);
      setSeller(updatedSellerRes.data);
    } catch (error) {
      console.error("Takip işlemi başarısız:", error);
      if (error.response?.status === 401) {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        navigate("/login");
      } else if (error.response?.status === 403) {
        alert("Bu işlem için yetkiniz yok. Lütfen BUYER hesabıyla giriş yapın.");
      } else {
        alert("Takip işlemi gerçekleştirilemedi. Lütfen daha sonra tekrar deneyin.");
      }
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <div className="error-message">{error}</div>
        <button className="back-button" onClick={() => navigate(-1)}>
          Geri Dön
        </button>
      </div>
    );
  }

  if (!seller) {
    return (
      <div className="error-container">
        <div className="error-message">Satıcı bulunamadı.</div>
        <button className="back-button" onClick={() => navigate(-1)}>
          Geri Dön
        </button>
      </div>
    );
  }

  return (
    <div className="seller-profile-container">
      <div className="seller-header">
        <div className="seller-info">
          <img
            src={
              seller.profilePicture
                ? `http://localhost:8080/profiles/${seller.profilePicture}`
                : "/default-avatar.png"
            }
            alt={seller.firstName}
            className="seller-avatar"
          />
          <div className="seller-details">
            <h1>
              {seller.firstName} {seller.lastName}
            </h1>
            <p className="seller-bio">
              {seller.bio || "Henüz biyografi eklenmemiş."}
            </p>
            <div className="seller-stats">
              <div className="stat">
                <span className="stat-value">{products.length}</span>
                <span className="stat-label">Ürün</span>
              </div>
              <div className="stat">
                <span className="stat-value">{seller.followerCount || 0}</span>
                <span className="stat-label">Takipçi</span>
              </div>
              <div className="stat">
                <span className="stat-value">{seller.rating || "0.0"}</span>
                <span className="stat-label">Puan</span>
              </div>
            </div>
            {isLoggedIn &&
              userRole === "BUYER" &&
              seller.userType === "SELLER" && (
                <button
                  className={`follow-button ${isFollowing ? "following" : ""}`}
                  onClick={handleToggleFollow}
                >
                  {isFollowing ? (
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
      </div>

      <div className="seller-products">
        <h2>Satıcının Ürünleri</h2>
        {products.length === 0 ? (
          <div className="no-products">
            <p>Henüz ürün bulunmuyor.</p>
          </div>
        ) : (
          <div className="products-grid">
            {products.map((product) => (
              <div
                key={product.id}
                className="product-card"
                onClick={() => navigate(`/products/${product.id}`)}
              >
                <div className="seller-product-image">
                  <img
                    src={
                      product.images && product.images.length > 0
                        ? `http://localhost:8080/uploads/products/${
                            product.id
                          }/${product.images[0].split("/").pop()}`
                        : "/default-product.png"
                    }
                    alt={product.title}
                  />
                </div>
                <div className="product-details">
                  <h3>{product.title}</h3>
                  <p className="product-description">
                    {product.description?.substring(0, 100)}
                    {product.description?.length > 100 ? "..." : ""}
                  </p>
                  <p className="product-category">{product.category?.name}</p>
                  <p className="product-price">{product.price} TL</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default SellerProfile; 
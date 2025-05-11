import React, { useState, useEffect } from "react";
import axios from "axios";
import "./Admin.css";

const Admin = () => {
  // Kullanıcı state'leri
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");
  const [userType, setUserType] = useState("ALL");

  // Ürün state'leri
  const [products, setProducts] = useState([]);
  const [productSearchTerm, setProductSearchTerm] = useState("");
  const [productCategoryFilter, setProductCategoryFilter] = useState("ALL");
  const [categories, setCategories] = useState([]);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [showEditForm, setShowEditForm] = useState(false);
  const [editFormData, setEditFormData] = useState({
    title: "",
    price: "",
    stock: "",
    status: "",
    categoryId: "",
  });
  const [successMessage, setSuccessMessage] = useState(null);

  // Panel seçimi
  const [activePanel, setActivePanel] = useState("users");

  useEffect(() => {
    if (activePanel === "users") {
      fetchUsers();
    } else {
      fetchProducts();
      fetchCategories();
    }
  }, [
    currentPage,
    userType,
    searchTerm,
    activePanel,
    productSearchTerm,
    productCategoryFilter,
  ]);

  const fetchUsers = async () => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        setError("Oturum süreniz dolmuş olabilir. Lütfen tekrar giriş yapın.");
        return;
      }

      const response = await axios.get(
        `http://localhost:8080/api/v1/admin/users?page=${currentPage}&size=10&userType=${userType}&search=${searchTerm}&sort=id,asc`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setUsers(response.data.content);
      setTotalPages(response.data.totalPages);
      setLoading(false);
    } catch (err) {
      console.error("Error fetching users:", err);
      if (err.response?.status === 403) {
        setError("Bu işlemi gerçekleştirmek için yetkiniz bulunmamaktadır.");
      } else if (err.response?.status === 401) {
        setError("Oturum süreniz dolmuş olabilir. Lütfen tekrar giriş yapın.");
      } else {
        setError("Kullanıcılar yüklenirken bir hata oluştu.");
      }
      setLoading(false);
    }
  };

  const fetchProducts = async () => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        setError("Oturum süreniz dolmuş olabilir. Lütfen tekrar giriş yapın.");
        return;
      }

      const response = await axios.get(
        `http://localhost:8080/api/v1/admin/products?page=${currentPage}&size=10&category=${productCategoryFilter}&search=${productSearchTerm}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setProducts(response.data.content);
      setTotalPages(response.data.totalPages);
      setLoading(false);
    } catch (err) {
      console.error("Error fetching products:", err);
      if (err.response?.status === 403) {
        setError("Bu işlemi gerçekleştirmek için yetkiniz bulunmamaktadır.");
      } else if (err.response?.status === 401) {
        setError("Oturum süreniz dolmuş olabilir. Lütfen tekrar giriş yapın.");
      } else {
        setError("Ürünler yüklenirken bir hata oluştu.");
      }
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        setError("Oturum süreniz dolmuş olabilir. Lütfen tekrar giriş yapın.");
        return;
      }

      const response = await axios.get(
        "http://localhost:8080/api/v1/categories",
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setCategories(response.data);
    } catch (err) {
      console.error("Error fetching categories:", err);
    }
  };

  const handleUserStatusChange = async (userId, currentStatus) => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        setError("Oturum süreniz dolmuş olabilir. Lütfen tekrar giriş yapın.");
        return;
      }

      const newStatus = currentStatus === "ACTIVE" ? "INACTIVE" : "ACTIVE";
      await axios.put(
        `http://localhost:8080/api/v1/admin/users/${userId}/status`,
        {
          status: newStatus,
          reason:
            newStatus === "ACTIVE"
              ? "Hesap aktifleştirildi"
              : "Hesap pasifleştirildi",
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      fetchUsers();
    } catch (err) {
      console.error("Error updating user status:", err);
      if (err.response?.status === 403) {
        setError("Bu işlemi gerçekleştirmek için yetkiniz bulunmamaktadır.");
      } else if (err.response?.status === 401) {
        setError("Oturum süreniz dolmuş olabilir. Lütfen tekrar giriş yapın.");
      } else {
        setError("Kullanıcı durumu güncellenirken bir hata oluştu.");
      }
    }
  };

  const handleEditClick = (product) => {
    setSelectedProduct(product);
    setEditFormData({
      title: product.title,
      price: product.price,
      stock: product.stock,
      status: product.status,
      categoryId: product.category.id,
    });
    setShowEditForm(true);
  };

  const handleProductUpdate = async () => {
    try {
      const token = localStorage.getItem("token");
      if (!token) {
        setError("Oturum süreniz dolmuş olabilir. Lütfen tekrar giriş yapın.");
        return;
      }

      const updateData = {
        title: editFormData.title,
        price: parseFloat(editFormData.price),
        stock: parseInt(editFormData.stock),
        status: editFormData.status,
        categoryId: parseInt(editFormData.categoryId),
      };

      const response = await axios.put(
        `http://localhost:8080/api/v1/admin/products/${selectedProduct.id}`,
        updateData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      setShowEditForm(false);
      await fetchProducts();
      setSuccessMessage("Ürün başarıyla güncellendi");
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      console.error("Update error details:", {
        message: err.message,
        status: err.response?.status,
        data: err.response?.data,
        headers: err.config?.headers,
      });

      if (err.response?.status === 403) {
        setError(
          "Bu işlemi gerçekleştirmek için yetkiniz bulunmamaktadır. Lütfen admin olarak giriş yaptığınızdan emin olun."
        );
      } else if (err.response?.status === 401) {
        setError("Oturum süreniz dolmuş olabilir. Lütfen tekrar giriş yapın.");
      } else {
        setError(
          err.response?.data?.message || "Ürün güncellenirken bir hata oluştu."
        );
      }
    }
  };

  const handleProductDelete = async (productId) => {
    if (window.confirm("Bu ürünü silmek istediğinizden emin misiniz?")) {
      try {
        const token = localStorage.getItem("token");
        console.log("Token:", token);
        console.log(
          "Request URL:",
          `http://localhost:8080/api/v1/admin/products/${productId}?reason=Admin tarafından silindi`
        );
        await axios.delete(
          `http://localhost:8080/api/v1/admin/products/${productId}?reason=Admin tarafından silindi`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
              "Content-Type": "application/json",
            },
          }
        );
        setProducts(products.filter((product) => product.id !== productId));
        setSuccessMessage("Ürün başarıyla silindi");
      } catch (err) {
        console.error("Error deleting product:", err);
        console.error("Error details:", {
          status: err.response?.status,
          data: err.response?.data,
          headers: err.response?.headers,
        });
        if (err.response?.status === 403) {
          setError("Bu işlemi gerçekleştirmek için yetkiniz bulunmamaktadır.");
        } else if (err.response?.status === 401) {
          setError(
            "Oturum süreniz dolmuş olabilir. Lütfen tekrar giriş yapın."
          );
        } else {
          setError("Ürün silinirken bir hata oluştu.");
        }
      }
    }
  };

  const handleSearch = (e) => {
    if (activePanel === "users") {
      setSearchTerm(e.target.value);
    } else {
      setProductSearchTerm(e.target.value);
    }
    setCurrentPage(0);
  };

  const handleFilterChange = (e) => {
    if (activePanel === "users") {
      setUserType(e.target.value);
    } else {
      setProductCategoryFilter(e.target.value);
    }
    setCurrentPage(0);
  };

  if (loading) return <div className="loading">Yükleniyor...</div>;
  if (error) return <div className="error-message">{error}</div>;

  return (
    <div className="admin-container">
      <div className="admin-header">
        <h2>Admin Paneli</h2>
        <div className="panel-selector">
          <select
            value={activePanel}
            onChange={(e) => setActivePanel(e.target.value)}
          >
            <option value="users">Kullanıcı Yönetimi</option>
            <option value="products">Ürün Yönetimi</option>
          </select>
        </div>
      </div>

      <div className="admin-controls">
        <div className="search-box">
          <input
            type="text"
            placeholder={
              activePanel === "users" ? "Kullanıcı ara..." : "Ürün ara..."
            }
            value={activePanel === "users" ? searchTerm : productSearchTerm}
            onChange={handleSearch}
          />
        </div>
        <div className="filter-box">
          <select
            value={activePanel === "users" ? userType : productCategoryFilter}
            onChange={handleFilterChange}
          >
            <option value="ALL">
              {activePanel === "users" ? "Tüm Kullanıcılar" : "Tüm Kategoriler"}
            </option>
            {activePanel === "users" ? (
              <>
                <option value="BUYER">Alıcılar</option>
                <option value="SELLER">Satıcılar</option>
                <option value="ADMIN">Adminler</option>
              </>
            ) : (
              categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))
            )}
          </select>
        </div>
      </div>

      {activePanel === "users" ? (
        <div className="users-table">
          <table>
            <thead>
              <tr>
                <th>Ad Soyad</th>
                <th>E-posta</th>
                <th>Kullanıcı Tipi</th>
                <th>Durum</th>
                <th>İşlemler</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>
                    {user.firstName} {user.lastName}
                  </td>
                  <td>{user.email}</td>
                  <td>
                    {user.userType === "BUYER" && "Alıcı"}
                    {user.userType === "SELLER" && "Satıcı"}
                    {user.userType === "ADMIN" && "Admin"}
                  </td>
                  <td>
                    <span
                      className={`status-badge ${
                        user.active ? "status-available" : "status-inactive"
                      }`}
                    >
                      {user.active ? "Aktif" : "Pasif"}
                    </span>
                  </td>
                  <td>
                    <button
                      className={
                        user.active ? "deactivate-btn" : "activate-btn"
                      }
                      onClick={() =>
                        handleUserStatusChange(
                          user.id,
                          user.active ? "ACTIVE" : "INACTIVE"
                        )
                      }
                    >
                      {user.active ? "Pasifleştir" : "Aktifleştir"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="products-table">
          <table>
            <thead>
              <tr>
                <th>Ürün Adı</th>
                <th>Satıcı</th>
                <th>Kategori</th>
                <th>Fiyat</th>
                <th>Stok</th>
                <th>Durum</th>
                <th>İşlemler</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id}>
                  <td>{product.title}</td>
                  <td>
                    {product.seller.firstName} {product.seller.lastName}
                  </td>
                  <td>{product.category.name}</td>
                  <td>{product.price} TL</td>
                  <td>{product.stock}</td>
                  <td>
                    <span
                      className={`status-badge ${
                        product.status === "AVAILABLE"
                          ? "status-available"
                          : product.status === "INACTIVE"
                          ? "status-inactive"
                          : product.status === "PENDING_REVIEW"
                          ? "status-pending"
                          : product.status === "REJECTED"
                          ? "status-rejected"
                          : "status-removed"
                      }`}
                    >
                      {product.status === "AVAILABLE" && "Aktif"}
                      {product.status === "INACTIVE" && "Pasif"}
                      {product.status === "PENDING_REVIEW" && "Onay Bekliyor"}
                      {product.status === "REJECTED" && "Reddedildi"}
                      {product.status === "REMOVED" && "Silindi"}
                    </span>
                  </td>
                  <td>
                    <button
                      className="edit-btn"
                      onClick={() => handleEditClick(product)}
                    >
                      Düzenle
                    </button>
                    <button
                      className="delete-btn"
                      onClick={() => handleProductDelete(product.id)}
                    >
                      Sil
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showEditForm && (
        <div className="edit-form-overlay">
          <div className="edit-form">
            <h3>Ürün Düzenle</h3>
            <div className="form-group">
              <label>Başlık:</label>
              <input
                type="text"
                value={editFormData.title}
                onChange={(e) =>
                  setEditFormData({ ...editFormData, title: e.target.value })
                }
              />
            </div>
            <div className="form-group">
              <label>Fiyat:</label>
              <input
                type="number"
                value={editFormData.price}
                onChange={(e) =>
                  setEditFormData({ ...editFormData, price: e.target.value })
                }
              />
            </div>
            <div className="form-group">
              <label>Stok:</label>
              <input
                type="number"
                value={editFormData.stock}
                onChange={(e) =>
                  setEditFormData({ ...editFormData, stock: e.target.value })
                }
              />
            </div>
            <div className="form-group">
              <label>Kategori:</label>
              <select
                value={editFormData.categoryId}
                onChange={(e) =>
                  setEditFormData({
                    ...editFormData,
                    categoryId: e.target.value,
                  })
                }
              >
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Durum:</label>
              <select
                value={editFormData.status}
                onChange={(e) =>
                  setEditFormData({ ...editFormData, status: e.target.value })
                }
              >
                <option value="AVAILABLE">Aktif</option>
                <option value="INACTIVE">Pasif</option>
                <option value="PENDING_REVIEW">Onay Bekliyor</option>
                <option value="REJECTED">Reddedildi</option>
                <option value="REMOVED">Silindi</option>
              </select>
            </div>
            <div className="form-buttons">
              <button onClick={handleProductUpdate}>Kaydet</button>
              <button onClick={() => setShowEditForm(false)}>İptal</button>
            </div>
          </div>
        </div>
      )}

      {successMessage && (
        <div className="success-message">{successMessage}</div>
      )}

      <div className="pagination">
        <button
          onClick={() => setCurrentPage(currentPage - 1)}
          disabled={currentPage === 0}
        >
          Önceki
        </button>
        <span>
          Sayfa {currentPage + 1} / {totalPages}
        </span>
        <button
          onClick={() => setCurrentPage(currentPage + 1)}
          disabled={currentPage === totalPages - 1}
        >
          Sonraki
        </button>
      </div>
    </div>
  );
};

export default Admin;

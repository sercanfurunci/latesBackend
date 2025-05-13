import React, { useEffect, useState } from "react";
import axios from "axios";
import { FaStar, FaTrash, FaUser, FaPhone, FaEnvelope } from "react-icons/fa";
import { useParams, useNavigate } from "react-router-dom";
import "./ProductDetail.css";

const API_URL = "http://localhost:8080";

const ProductDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [rating, setRating] = useState(5);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userRole, setUserRole] = useState("");
  const [userId, setUserId] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [replyText, setReplyText] = useState({});
  const [editingReply, setEditingReply] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("userType");
    const uid = localStorage.getItem("userId");
    setIsLoggedIn(!!token);
    setUserRole(role || "");
    setUserId(uid || "");

    const fetchProduct = async () => {
      if (!id) {
        setError("Ürün ID'si bulunamadı");
        setLoading(false);
        return;
      }

      try {
        const response = await axios.get(`${API_URL}/api/v1/products/${id}`, {
          headers: token ? { Authorization: `Bearer ${token}` } : {},
        });
        setProduct(response.data);
      } catch (error) {
        console.error("Ürün yüklenirken hata:", error);
        setError("Ürün yüklenirken bir hata oluştu");
      } finally {
        setLoading(false);
      }
    };

    const fetchComments = async () => {
      if (!id) return;

      try {
        const response = await axios.get(
          `${API_URL}/api/v1/buyer/comments/product/${id}`
        );
        setComments(response.data);
      } catch (error) {
        console.error("Yorumlar yüklenirken hata:", error);
      }
    };

    fetchProduct();
    fetchComments();
  }, [id]);

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!isLoggedIn) {
      alert("Yorum yapabilmek için giriş yapmalısınız");
      return;
    }

    if (!newComment.trim()) {
      alert("Lütfen bir yorum yazın");
      return;
    }

    try {
      const token = localStorage.getItem("token");
      const response = await axios.post(
        `${API_URL}/api/v1/buyer/comments`,
        {
          productId: id,
          content: newComment,
          rating: rating,
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      setComments([response.data, ...comments]);
      setNewComment("");
      setRating(5);
    } catch (error) {
      console.error("Yorum eklenirken hata:", error);
      alert(
        error.response?.data?.message || "Yorum eklenirken bir hata oluştu"
      );
    }
  };

  const handleAddReply = async (commentId) => {
    if (!replyText[commentId]?.trim()) {
      alert("Lütfen bir yanıt yazın");
      return;
    }

    try {
      const token = localStorage.getItem("token");
      const response = await axios.post(
        `${API_URL}/api/v1/buyer/comments/${commentId}/reply`,
        {
          reply: replyText[commentId],
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      setComments(
        comments.map((comment) =>
          comment.id === commentId ? response.data : comment
        )
      );
      setReplyText({ ...replyText, [commentId]: "" });
      setEditingReply(null);
    } catch (error) {
      console.error("Yanıt eklenirken hata:", error);
      alert(
        error.response?.data?.message || "Yanıt eklenirken bir hata oluştu"
      );
    }
  };

  const handleEditReply = async (commentId) => {
    if (!replyText[commentId]?.trim()) {
      alert("Lütfen bir yanıt yazın");
      return;
    }

    try {
      const token = localStorage.getItem("token");
      const response = await axios.post(
        `${API_URL}/api/v1/buyer/comments/${commentId}/reply`,
        {
          reply: replyText[commentId],
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      setComments(
        comments.map((comment) =>
          comment.id === commentId ? response.data : comment
        )
      );
      setReplyText({ ...replyText, [commentId]: "" });
      setEditingReply(null);
    } catch (error) {
      console.error("Yanıt güncellenirken hata:", error);
      alert(
        error.response?.data?.message || "Yanıt güncellenirken bir hata oluştu"
      );
    }
  };

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm("Bu yorumu silmek istediğinizden emin misiniz?")) {
      return;
    }

    try {
      const token = localStorage.getItem("token");
      await axios.delete(`${API_URL}/api/v1/buyer/comments/${commentId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setComments(comments.filter((comment) => comment.id !== commentId));
    } catch (error) {
      console.error("Yorum silinirken hata:", error);
      alert(
        error.response?.data?.message || "Yorum silinirken bir hata oluştu"
      );
    }
  };

  if (loading) {
    return <div className="loading">Yükleniyor...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  if (!product) {
    return <div className="error">Ürün bulunamadı</div>;
  }

  return (
    <div className="product-detail">
      <div className="product-header">
        <h1>{product.title}</h1>
        <div className="product-meta">
          <span className="price">{product.price} TL</span>
          <span className="category">{product.category.name}</span>
          <span className="stock">Stok: {product.stock}</span>
        </div>
      </div>

      <div className="product-content">
        <div className="product-images">
          {product.images && product.images.length > 0 ? (
            <img
              src={`${API_URL}${product.images[0]}`}
              alt={product.title}
              onError={(e) => {
                e.target.onerror = null;
                e.target.src = "/placeholder-image.jpg";
              }}
            />
          ) : (
            <div className="no-image">Resim yok</div>
          )}
        </div>

        <div className="product-info">
          <div className="product-description">
            <h2>Ürün Açıklaması</h2>
            <p>{product.description}</p>
          </div>

          <div className="seller-info">
            <h2>Satıcı Bilgileri</h2>
            <div className="seller-profile">
              <img
                src={
                  product.seller?.profilePicture
                    ? `http://localhost:8080/profiles/${product.seller.profilePicture}`
                    : "/default-avatar.png"
                }
                alt={`${product.seller?.firstName} ${product.seller?.lastName}`}
                className="seller-avatar"
                onClick={() => navigate(`/seller/${product.seller.id}`)}
                style={{ cursor: "pointer" }}
                onError={(e) => {
                  e.target.onerror = null;
                  e.target.src = "/default-avatar.png";
                }}
              />
              <div className="seller-details">
                <h3
                  className="seller-name"
                  onClick={() => navigate(`/seller/${product.seller.id}`)}
                  style={{ cursor: "pointer" }}
                >
                  {product.seller?.firstName} {product.seller?.lastName}
                </h3>
                <p className="seller-bio">{product.seller?.bio}</p>
                <div className="seller-contact">
                  <span>
                    <FaPhone /> {product.seller?.phoneNumber}
                  </span>
                  <span>
                    <FaEnvelope /> {product.seller?.email}
                  </span>
                </div>
              </div>
            </div>
          </div>

          {product.ingredients && (
            <div className="product-ingredients">
              <h2>İçindekiler</h2>
              <p>{product.ingredients}</p>
            </div>
          )}

          {product.preparationTime && (
            <div className="product-preparation">
              <h2>Hazırlama Süresi</h2>
              <p>{product.preparationTime}</p>
            </div>
          )}
        </div>
      </div>

      <div className="comments-section">
        <h2>Yorumlar</h2>
        {isLoggedIn && userRole !== "SELLER" && (
          <form onSubmit={handleAddComment} className="comment-form">
            <div className="rating-input">
              <label>Puanınız:</label>
              <div className="star-rating">
                {[1, 2, 3, 4, 5].map((star) => (
                  <FaStar
                    key={star}
                    className={`star-icon ${star <= rating ? "active" : ""}`}
                    onClick={() => setRating(star)}
                    style={{ cursor: "pointer" }}
                  />
                ))}
              </div>
            </div>
            <textarea
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="Yorumunuzu yazın..."
              required
            />
            <button type="submit">Yorum Yap</button>
          </form>
        )}

        <div className="comments-list">
          {comments.length === 0 ? (
            <p className="no-comments">Henüz yorum yapılmamış</p>
          ) : (
            comments.map((comment) => (
              <div key={comment.id} className="comment">
                <div className="comment-header">
                  <span className="comment-author">
                    {comment.user.firstName} {comment.user.lastName}
                  </span>
                  <span className="comment-date">
                    {new Date(comment.createdAt).toLocaleDateString()}
                  </span>
                </div>
                <div className="comment-content">
                  <div className="comment-rating">
                    {[...Array(comment.rating)].map((_, i) => (
                      <FaStar key={i} className="star-icon" />
                    ))}
                  </div>
                  <p>{comment.content}</p>
                  {comment.sellerReply && !editingReply && (
                    <div className="seller-reply">
                      <strong>Satıcı Yanıtı:</strong>
                      <p>{comment.sellerReply}</p>
                      {userRole === "SELLER" &&
                        String(product.seller.id) === userId && (
                          <button
                            className="edit-reply"
                            onClick={() => {
                              setEditingReply(comment.id);
                              setReplyText({
                                ...replyText,
                                [comment.id]: comment.sellerReply,
                              });
                            }}
                          >
                            Düzenle
                          </button>
                        )}
                    </div>
                  )}
                  {userRole === "SELLER" &&
                    String(product.seller.id) === userId && (
                      <div className="reply-section">
                        {editingReply === comment.id ? (
                          <>
                            <textarea
                              value={replyText[comment.id] || ""}
                              onChange={(e) =>
                                setReplyText({
                                  ...replyText,
                                  [comment.id]: e.target.value,
                                })
                              }
                              placeholder="Yanıtınızı yazın..."
                            />
                            <div className="reply-buttons">
                              <button
                                onClick={() => handleEditReply(comment.id)}
                              >
                                Güncelle
                              </button>
                              <button
                                onClick={() => {
                                  setEditingReply(null);
                                  setReplyText({
                                    ...replyText,
                                    [comment.id]: "",
                                  });
                                }}
                              >
                                İptal
                              </button>
                            </div>
                          </>
                        ) : !comment.sellerReply ? (
                          <>
                            <textarea
                              value={replyText[comment.id] || ""}
                              onChange={(e) =>
                                setReplyText({
                                  ...replyText,
                                  [comment.id]: e.target.value,
                                })
                              }
                              placeholder="Yanıtınızı yazın..."
                            />
                            <button onClick={() => handleAddReply(comment.id)}>
                              Yanıtla
                            </button>
                          </>
                        ) : null}
                      </div>
                    )}
                </div>
                {(userRole === "SELLER" &&
                  String(product.seller.id) === userId) ||
                comment.user.id === userId ? (
                  <button
                    className="delete-comment"
                    onClick={() => handleDeleteComment(comment.id)}
                  >
                    <FaTrash /> Sil
                  </button>
                ) : null}
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;

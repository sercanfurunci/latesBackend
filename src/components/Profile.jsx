import React, { useState } from "react";
import axios from "axios";

const Profile = () => {
  const [user, setUser] = useState({});
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState("");

  const handleFileChange = async (e) => {
    const file = e.target.files[0];
    if (file) {
      setSelectedFile(file);
      // Önizleme URL'i oluştur
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSaveProfilePicture = async () => {
    if (!selectedFile) return;

    try {
      setIsUploading(true);
      const token = localStorage.getItem("token");
      const formData = new FormData();
      formData.append("profilePicture", selectedFile);

      const response = await axios.post(
        `http://localhost:8080/api/v1/users/${id}/profile-picture`,
        formData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "multipart/form-data",
          },
        }
      );

      setUser(response.data);
      setSelectedFile(null);
      setPreviewUrl(null);
      setIsUploading(false);
    } catch (error) {
      console.error("Profil fotoğrafı yüklenirken hata:", error);
      setError("Profil fotoğrafı yüklenirken bir hata oluştu.");
      setIsUploading(false);
    }
  };

  const handleCancelUpload = () => {
    setSelectedFile(null);
    setPreviewUrl(null);
  };

  return (
    <div className="profile-container">
      <div className="profile-header">
        <div className="profile-avatar">
          <img
            src={
              previewUrl ||
              (user.profilePicture
                ? `http://localhost:8080/profiles/${user.profilePicture}`
                : "/default-avatar.png")
            }
            alt="Profil"
            className="profile-image"
          />
          {editing && (
            <div className="profile-image-upload">
              <label htmlFor="profile-picture" className="upload-label">
                <i className="fas fa-camera"></i> Fotoğraf Değiştir
              </label>
              <input
                id="profile-picture"
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                className="file-input"
              />
            </div>
          )}
        </div>
        {previewUrl && (
          <div className="profile-picture-actions">
            <button
              className="save-picture-button"
              onClick={handleSaveProfilePicture}
              disabled={isUploading}
            >
              {isUploading ? (
                <>
                  <i className="fas fa-spinner fa-spin"></i> Yükleniyor...
                </>
              ) : (
                <>
                  <i className="fas fa-save"></i> Kaydet
                </>
              )}
            </button>
            <button
              className="cancel-picture-button"
              onClick={handleCancelUpload}
              disabled={isUploading}
            >
              <i className="fas fa-times"></i> İptal
            </button>
          </div>
        )}
        <h2>
          {user.firstName} {user.lastName}
        </h2>
      </div>
    </div>
  );
};

export default Profile;

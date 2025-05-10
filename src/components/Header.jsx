<button
  className="user-menu-button"
  onClick={() => setShowUserMenu(!showUserMenu)}
>
  <img
    src={
      user.profilePicture
        ? `http://localhost:8080/profiles/${user.profilePicture}`
        : "/default-avatar.png"
    }
    alt="Profil"
    className="user-avatar"
  />
  <span>{user.firstName}</span>
</button>;

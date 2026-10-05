const cover = document.getElementById('cover');
  const invite = document.getElementById('invite');
  const btn = document.getElementById('openBtn');

  btn.addEventListener('click', () => {
    cover.classList.add('opened');
    invite.classList.add('show');
    btn.disabled = true;
  });

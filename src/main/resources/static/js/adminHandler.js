
    // Получаем CSRF токен из meta-тегов
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    // Находим форму по id
    const form = $('#editForm');
    const submitButton = form.find('button[type="submit"]'); // Кнопка отправки
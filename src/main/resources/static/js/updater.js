
// Форма редактирования данных юзера
$(document).ready(function () {


    // Функция для показа сообщения
    function showSuccessMessage() {
        $('#successEditeMessage').fadeIn(); // Плавно показываем сообщение
        setTimeout(function () {
            $('#successEditeMessage').fadeOut(); // Плавно скрываем через 3 секунды
        }, 5000);
    }

    // Маска для телефона, только если элемент существует
    if ($('input[name="phone"]').length) {
        $('input[name="phone"]').inputmask("+7 (999) 999-99-99");
    }

    // Получаем CSRF токен из meta-тегов
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    // Находим форму по id
    const form = $('#editForm');
    const submitButton = form.find('button[type="submit"]'); // Кнопка отправки
    const originalButtonText = submitButton.html(); // Сохраняем оригинальный текст кнопки
    const errorMessage = $('#errorEditeMessage');
    const successMessage = $('#successEditeMessage')

    successMessage.hide();

    $('#editForm').on('submit', function (event) {

        // Предотвращаем отправку формы
        event.preventDefault();

        errorMessage.hide();

        // Собираем данные формы
        const formData = {
            fio: $('#fioEditing').val(),
            phone: $('#phoneEditing').val(),
            nameSalon: $('#salonEditing').val(),
            city: $('#cityEditing').val(),
            address: $('#addressEditing').val()
        };

        if($('#innEditing')) {
            formData.innCompany = $('#innEditing').val();
        }

        // Меняем текст кнопки на спиннер и блокируем её
        submitButton.html(`
            <span class="spinner-grow spinner-grow-sm" role="status" aria-hidden="true"></span>
            Отправка...
        `);
        submitButton.prop('disabled', true);

        // Отправляем данные через AJAX
        $.ajax({
            type: form.attr('method'), // Используем метод, указанный в форме
            url: form.attr('action'), // Используем URL, указанный в форме
            contentType: 'application/json', // Тип данных
            data: JSON.stringify(formData), // Преобразуем данные в JSON
            beforeSend: function(xhr) {
            // Добавляем CSRF токен в заголовок запроса
            xhr.setRequestHeader(csrfHeader, csrfToken);
            },
            success: function (response) {
            // Обработка успешного ответа
                if (response.redirectUrl) {
                    // Перенаправляем пользователя на указанный URL
                    window.location.href = response.redirectUrl;
                } else {

                    // вызов функции при успешной операции
                    showSuccessMessage();

                }
            },
            error: function (xhr) {
                let errorText = 'Произошла ошибка при отправке формы.';
                try {
                    // Пробуем распарсить JSON из ответа
                    const responseJson = JSON.parse(xhr.responseText);

                    // Если поле message существует, используем его значение
                    if (responseJson.message) {
                        errorText = responseJson.message;
                    }
                } catch (e) {
                    console.error('Ошибка при парсинге JSON:', e);
                }
                // Показываем сообщение об ошибке
                errorMessage.text(errorText).fadeIn();
            },
            complete: function () {
                // Восстанавливаем текст кнопки и разблокируем её
                submitButton.html(originalButtonText);
                submitButton.prop('disabled', false);
            }
        });
    });
});
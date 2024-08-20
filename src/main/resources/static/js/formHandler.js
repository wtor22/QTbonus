$(document).ready(function () {

// Получаем CSRF токен из meta-тегов
const csrfToken = $('meta[name="_csrf"]').attr('content');
const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

console.log('CSRF Token:', csrfToken);
console.log('CSRF Header:', csrfHeader);

    // Находим форму по id
    const form = $('#regForm');
    const successMessage = $('#successMessage');
    const errorMessage = $('#errorMessage');



    form.on('submit', function (event) {
        // Предотвращаем отправку формы
        event.preventDefault();
        // Собираем данные формы
        const formData = {
            token: $('#token').val(),
            email: $('#emailReg').val(),
            fio: $('#fioInput').val(),
            phone: $('#phoneInput').val(),
            nameSalon: $('#saloonInput').val(),
            address: $('#addressInput').val(),
            manager: $('#manager').val(),
            password: $('#password').val()
        };
        console.log('Отправка данных формы:', formData);
                // Скрываем предыдущие сообщения
                successMessage.hide();
                errorMessage.hide();
        // Отправляем данные через AJAX
        $.ajax({
            //type: 'POST',  // Явно указываем метод POST
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
            console.log('Ответ сервера:', response);

                if (response.redirectUrl) {
                    // Перенаправляем пользователя на указанный URL
                    window.location.href = response.redirectUrl;
                } else {
                    successMessage.fadeIn(); // Показываем сообщение об успехе
                }

//            successMessage.fadeIn(); // Показываем сообщение об успехе

             // Закрываем Offcanvas
             var offcanvasElement = document.getElementById('offcanvasRegistry');
             var bsOffcanvas = bootstrap.Offcanvas.getInstance(offcanvasElement);
             bsOffcanvas.hide();
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
            }
        });
    });
});

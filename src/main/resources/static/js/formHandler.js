
// Положение Offcanvas
function adjustOffcanvasPosition() {
    var offcanvasElement = document.getElementById('offcanvasInput');
    var offcanvasElementRegistry = document.getElementById('offcanvasRegistry');
    var offcanvasCreateOrder = document.getElementById('offcanvasCreateOrder');

    // Проверяем ширину экрана
    if (window.innerWidth < 1200) {
        if(offcanvasElement) {
          offcanvasElement.classList.remove('offcanvas-end');
          offcanvasElement.classList.add('offcanvas-top');
        }
        if(offcanvasElementRegistry) {
          offcanvasElementRegistry.classList.remove('offcanvas-end');
          offcanvasElementRegistry.classList.add('offcanvas-top');
        }
    } else {
        if(offcanvasElement) {
          offcanvasElement.classList.remove('offcanvas-top');
          offcanvasElement.classList.add('offcanvas-end');
        }
        if(offcanvasElementRegistry) {
          offcanvasElementRegistry.classList.remove('offcanvas-top');
          offcanvasElementRegistry.classList.add('offcanvas-end');
        }
    }
}
// Выполняем при загрузке страницы и при изменении размера окна
window.addEventListener('load', adjustOffcanvasPosition);
window.addEventListener('resize', adjustOffcanvasPosition);

$(document).ready(function () {

    // Форма входа
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    // Находим форму по id
    const form = $('#inputForm');
    const successMessage = $('#successRegMessage');
    const errorMessage = $('#errorInputMessage');

    form.on('submit', function (event) {
        // Предотвращаем отправку формы
        event.preventDefault();

        // Собираем данные формы
        const formData = {
            _csrf: csrfToken,
            username: $('#emailInput').val(),
            password: $('#passInput').val()
        };

        // Скрываем предыдущие сообщения
        successMessage.hide();
        errorMessage.hide();

        // Отправляем данные через AJAX
        $.ajax({
            type: 'POST',
            url: '/login',
            contentType: 'application/x-www-form-urlencoded',
            data: $.param(formData),
            beforeSend: function(xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            },
            success: function (response) {
                // Обработка успешного ответа
                // Перенаправляю на главную страницу
                window.location.href = "/";

                // Закрываем Offcanvas
                var offcanvasElement = document.getElementById('offcanvasInput');
                var bsOffcanvas = bootstrap.Offcanvas.getInstance(offcanvasElement);
                bsOffcanvas.hide();
            },
            error: function (xhr) {
                let errorText = 'Произошла ошибка при отправке формы.';
                try {
                    // Пробуем распарсить JSON из ответа
                    const responseJson = JSON.parse(xhr.responseText);

                    // Если поле error существует в ответе, используем его значение
                    if (responseJson.error) {
                        errorText = responseJson.error;
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

// Форма регистрации общая
$(document).ready(function () {

// Маска для телефона, только если элемент существует
if ($('input[name="phone"]').length) {
    $('input[name="phone"]').inputmask("+7 (999) 999-99-99");
}

// Получаем CSRF токен из meta-тегов
const csrfToken = $('meta[name="_csrf"]').attr('content');
const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    // Находим форму по id
    const form = $('#regForm');
    const buttonSuccessHide = $('#button-success-hide');
    const blockSuccessFade = $('#block-success-fade')
    const successMessage = $('#successRegMessage');
    const errorMessage = $('#errorMessage');
    const submitButton = form.find('button[type="submit"]'); // Кнопка отправки
    const originalButtonText = submitButton.html(); // Сохраняем оригинальный текст кнопки

    // Скрываем предыдущие сообщения
    successMessage.hide();
    errorMessage.hide();

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
//            console.log('Ответ сервера:', response);

                if (response.redirectUrl) {
                    // Перенаправляем пользователя на указанный URL
                    window.location.href = response.redirectUrl;
                } else {
                    successMessage.fadeIn(); // Показываем сообщение об успехе

                    //Для формы на странице регистраций по менеджерам
                    buttonSuccessHide.hide(); // Скрываем кнопку регистрации
                    blockSuccessFade.fadeIn();
                }
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
            },
            complete: function () {
                // Восстанавливаем текст кнопки и разблокируем её
                submitButton.html(originalButtonText);
                submitButton.prop('disabled', false);

            }
        });
    });
});




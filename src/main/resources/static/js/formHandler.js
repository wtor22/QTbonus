
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
//        if(offcanvasCreateOrder) {
//          offcanvasCreateOrder.classList.remove('offcanvas-end');
//          offcanvasCreateOrder.classList.add('offcanvas-top');
//        }
    } else {
        if(offcanvasElement) {
          offcanvasElement.classList.remove('offcanvas-top');
          offcanvasElement.classList.add('offcanvas-end');
        }
        if(offcanvasElementRegistry) {
          offcanvasElementRegistry.classList.remove('offcanvas-top');
          offcanvasElementRegistry.classList.add('offcanvas-end');
        }
//        if(offcanvasCreateOrder) {
//          offcanvasCreateOrder.classList.remove('offcanvas-top');
//          offcanvasCreateOrder.classList.add('offcanvas-end');
//        }
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
            contentType: 'application/x-www-form-urlencoded', // Указываем тип контента
            data: $.param(formData), // Преобразуем данные в URL-кодированную строку
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
//        console.log('Отправка данных формы:', formData);

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

// Переменная для проверки валидности номера счета
let isInvoiceValid = false;

// External Invoice Id STRING
let externalInvoiceId = "";
// Сумма Ордера
let price = 0.00;

// Очистка всех полей
function clearAllFields() {
    document.getElementById("innNumber").value = "";
    document.getElementById("invoiceNumber").value = "";
    document.getElementById("invoiceDate").value = "";
    document.getElementById("productName").value = "";
    document.getElementById("productExternalId").value = "";
    document.getElementById("productQuantity").value = "";
    document.getElementById("fieldInvoiceExternalId").value = "";
}


// Проверка ИНН
function validateInn() {
    let innNumber = document.getElementById("innNumber").value;
    let invoiceNumber = document.getElementById("invoiceNumber");
    let invoiceDate = document.getElementById("invoiceDate");
    let productName = document.getElementById("productName");
    let productExternalId = document.getElementById("productExternalId");
    let productQuantity = document.getElementById("productQuantity");
    let fieldInvoiceExternalId = document.getElementById("fieldInvoiceExternalId");
    let successOrderMessage = document.getElementById("successOrderCreated");

    if (innNumber) {
        let params = new URLSearchParams({innNumber: innNumber});
        successOrderMessage.style.display = "none";

        fetch('/order/validate-agent?' + params.toString())
            .then(response => {
                if (!response.ok) {
                    isInvoiceValid = false; // Сбрасываем флаг валидности
                    return response.text().then(text => { throw new Error(text) });
                }
                isInvoiceValid = true; // Если инн существует, устанавливаем флаг в true
                invoiceNumber.value = "";
                invoiceDate.value = "";
                productName.value = "";
                productExternalId.value = "";
                productQuantity.value = "";
                fieldInvoiceExternalId = "";
                innErrorForm.textContent = "";  // Убираем сообщение об ошибке если номер и дата валидны
            })
            .catch(error => {
                isInvoiceValid = false; // Сбрасываем флаг валидности
                innErrorForm.textContent = error.message;  // Выводим сообщение об ошибке

            });
    }
}

function validateInvoice() {
    let invoiceNumber = document.getElementById("invoiceNumber").value;
    let invoiceDate = document.getElementById("invoiceDate").value;
    let invoiceError = document.getElementById("invoiceError");
    let invoiceExternalId = document.getElementById("fieldInvoiceExternalId");
    let innNumber = document.getElementById("innNumber").value;

    if (invoiceNumber && invoiceDate) {
        let params = new URLSearchParams({ agentInn: innNumber,
                                            invoiceNumber: invoiceNumber,
                                            invoiceDate: invoiceDate });
        let productName = document.getElementById("productName").value;
        fetch('/order/validate-invoice?' + params.toString())
            .then(response => {
               return response.text().then(text => {
                   if (!response.ok) {
                      externalInvoiceId = "";
                      throw new Error(text); // Бросаем ошибку с текстом ответа
                   }
                   if(productName) {
                        validateProductInInvoice();
                   }
                   externalInvoiceId = text;
                   return text; // Возвращаем текст ответа
               });
            })
                .then(invoiceExternalId => {
                    fieldInvoiceExternalId.value = invoiceExternalId;
                    // Здесь можно использовать invoiceExternalId для дальнейшей обработки
                    invoiceError.textContent = ""; // очищаю текст в сообщение об ошибке

                })
            .catch(error => {
                isInvoiceValid = false; // Сбрасываем флаг валидности
                externalInvoiceId = "";
                invoiceError.textContent = error.message;  // Выводим сообщение об ошибке
            });
    }
}

 function searchProducts() {
        let productName = document.getElementById("productName").value;

        if (productName.length >= 4) { // Запуск поиска после ввода 4-го символа
            fetch('/order/search-products?query=' + productName)
                .then(response => response.json())
                .then(data => {
                    let suggestions = document.getElementById("productSuggestions");
                    suggestions.innerHTML = '';
                   if (data.length > 0) {
                        suggestions.style.display = 'block';
                        data.forEach(function(product) {
                            let li = document.createElement('li');
                            li.textContent = product.name;
                            li.onclick = function() {
                                document.getElementById("productName").value = product.name;
                                document.getElementById("productExternalId").value = product.externalId
                                suggestions.style.display = 'none';
                            };
                            suggestions.appendChild(li);
                        });
                    } else {
                        suggestions.style.display = 'none';
                    }
                });
        } else {
            document.getElementById("productSuggestions").style.display = 'none';
        }
 }

 // Validate product in invoice
 function validateProductInInvoice() {
 setTimeout(() => {  // Задержка перед выполнением основного кода
     let productName = document.getElementById("productName").value;
     let productQuantity = document.getElementById("productQuantity");

     if (productName && externalInvoiceId) {
         let params = new URLSearchParams({productName: productName, invoiceExternalId: externalInvoiceId});

         fetch('/order/search-products-in-invoice?' + params.toString())
             .then(response => {
                 if (!response.ok) {
                     isInvoiceValid = false; // Сбрасываем флаг валидности
                     document.getElementById("productExternalId").value = "";
                     return response.text().then(text => { throw new Error(text) });
                 }
                 productQuantity.value = "";
                 isInvoiceValid = true; // Если все О.К., устанавливаем флаг в true
                 productError.textContent = "";  // Убираем сообщение об ошибке если номер и дата валидны
             })
             .catch(error => {
                 isInvoiceValid = false; // Сбрасываем флаг валидности
                 document.getElementById("productExternalId").value = "";
                 productError.textContent = error.message;  // Выводим сообщение об ошибке
             });
     } else {
        productError.textContent = "";  // Убираем сообщение об ошибке если нет номера и имени продукта
        productQuantityError.textContent = ""; // Сбрасываю ошибку в проверке количества товаров
        document.getElementById("productExternalId").value = "";
        document.getElementById("productQuantity").value = "";
     }
 }, 500); // Задержка в 500 миллисекунд
 }

  // Validate product quantity in invoice
  function validateProductQuantityInInvoice() {
      let fieldInvoiceExternalId = document.getElementById("fieldInvoiceExternalId").value;
      let productExternalId = document.getElementById("productExternalId").value
      let productQuantity = document.getElementById("productQuantity").value

      if(!productQuantity && productExternalId) {
        productQuantityError.textContent = "Поле должно быть заполнено";
        isInvoiceValid = false; // Сбрасываем флаг валидности
        return
      }
      if(productQuantity <= 0 && productExternalId) {
         productQuantityError.textContent = "Количество товара должно быть больше чем 0";
         isInvoiceValid = false; // Сбрасываем флаг валидности
         return
      }
      if (externalInvoiceId && productExternalId) {
          let params = new URLSearchParams({productExternalId: productExternalId,
                                            productQuantity: productQuantity,
                                            invoiceExternalId: externalInvoiceId});
          fetch('/order/search-quantity-products-in-invoice?' + params.toString())
              .then(response => {
                  if (!response.ok) {
                      isInvoiceValid = false; // Сбрасываем флаг валидности
                      return response.text().then(text => { throw new Error(text) });
                  }
                  return response.text(); // Возвращаем текст ответа для следующего then
              })
                .then(text => {
                  // Преобразуем текст в число, если это цена
                  price = parseFloat(text);

                  isInvoiceValid = true; // Если количество существует, устанавливаем флаг в true
                  productQuantityError.textContent = "";
                  productError.textContent = ""; // Убираем сообщение об ошибке если номер и дата валидны
                })
              .catch(error => {
                  isInvoiceValid = false; // Сбрасываем флаг валидности
                  productQuantityError.textContent = error.message;  // Выводим сообщение об ошибке
              });
      } else {
        productError.textContent = "";  // Убираем сообщение об ошибке если очистилось поле ввода названия товара
        productQuantityError.textContent = ""; // Убираем сообщение об ошибке если очистилось поле ввода названия товара
      }
  }
// Слушаем события на элементе где изменяется количество товара
document.addEventListener('DOMContentLoaded', () => {
    const productQuantityInput = document.getElementById('productQuantity');

    if (productQuantityInput) {
        productQuantityInput.addEventListener('input', validateProductQuantityInInvoice);
    }
});

// Форма создания ордера
 $(document).ready(function () {
 // Получаем CSRF токен из meta-тегов
 const csrfToken = $('meta[name="_csrf"]').attr('content');
 const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

 // Находим форму по id
 const form = $('#orderForm');
 const successMessage =$('#successOrderCreated');
 const errorMessage = $('#errorMessage');
 const submitButton = form.find('button[type="submit"]'); // Кнопка отправки
 const originalButtonText = submitButton.html(); // Сохраняем оригинальный текст кнопки

 // Устанавливаем новое текстовое содержимое и показываем элемент
 successMessage.hide();
 errorMessage.hide();

     form.on('submit', function (event) {
         // Предотвращаем отправку формы
         event.preventDefault();

         // Проверяем флаг валидации
          if (!isInvoiceValid) {
          errorMessage.text('Пожалуйста, проверьте номер счета и дату.').fadeIn();
          return; // Прекращаем выполнение функции, чтобы не отправлять форму
          }

          let paymentOption = parseInt(document.querySelector('input[name="paymentOption"]:checked').value, 10);

         // Собираем данные формы
         const formData = {
             token: $('#token').val(),
             invoiceNumber: $('#invoiceNumber').val(),
             invoiceDate: $('#invoiceDate').val(),
             invoiceExternalId: $('#fieldInvoiceExternalId').val(),
             productExternalId: $('#productExternalId').val(),
             productName: $('#productName').val(),
             productQuantity: $('#productQuantity').val(),
             sum: price,
             paymentType: {
                id: paymentOption
             }
         };
//         console.log('Отправка данных формы:', formData);
//                 // Скрываем предыдущие сообщения
                 successMessage.hide();
                 errorMessage.hide();

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
             // Чищу поля
             clearAllFields()
             // Показываем сообщение об успехе
             successMessage.fadeIn();
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

// AJAX обновления списка ордеров
document.addEventListener('DOMContentLoaded', function() {
        function loadOrders() {

                let ordersTable = document.getElementById('ordersTable');
                if (!ordersTable) {
                    console.warn("Элемент 'ordersTable' не найден!");
                    return;
                }
            fetch('/order')
                .then(response => response.json())
                .then(data => {
                    let ordersTable = document.getElementById('ordersTable');

                    ordersTable.innerHTML = ''; // Очистка таблицы перед добавлением новых данных

                    if (data.length > 0) {
                        data.forEach(order => {
                            let row = document.createElement('tr');
                            // Создание элемента <span> с классом из order.status.color
                            let statusSpan = `<span class="badge ${order.statusColor}">${order.statusName}</span>`;

                            row.innerHTML = `
                                <td>${order.createDate}</td>
                                <td>${order.sum}</td>
                                <td>${statusSpan}</td>
                                <td>Счёт №${order.invoiceNumber} от ${order.invoiceDate}</td>
                                <td>${order.productName}</td>
                                <td>${order.productQuantity}</td>
                            `;
                            ordersTable.appendChild(row);
                        });
                    } else {
                        ordersTable.innerHTML = '<tr><td colspan="3">Нет заказов для отображения.</td></tr>';
                    }
                })
                .catch(error => console.error('Ошибка при загрузке заказов:', error));
        }
                // Загружаем заказы через таймаут, чтобы убедиться, что HTML загружен
                setTimeout(loadOrders, 100); // 100 миллисекунд
                //loadOrders();

                // Также загружаем заказы при клике на вкладку "Home"
                const homeTab = document.getElementById('home-tab');
                homeTab.addEventListener('click', loadOrders);

});

// JavaScript для управления отображением форм выбора способа оплаты
document.addEventListener('DOMContentLoaded', function() {
    const phoneOption = document.getElementById('phoneOption');
    const bankOption = document.getElementById('bankOption');
    const phoneForm = document.getElementById('phoneForm');
    const bankForm = document.getElementById('bankForm');

    function updateFormVisibility() {
        if (phoneOption.checked) {
            phoneForm.style.display = 'block';
            bankForm.style.display = 'none';
        } else {
            phoneForm.style.display = 'none';
            bankForm.style.display = 'block';
        }
    }

    // Обработчики событий для переключения радио кнопок
    phoneOption.addEventListener('change', updateFormVisibility);
    bankOption.addEventListener('change', updateFormVisibility);

    // Изначально обновляем видимость форм в зависимости от выбранного варианта
    updateFormVisibility();
});

// JavaScript для управления отображением форм выбора типа Покупателя
document.addEventListener('DOMContentLoaded', function() {
    const llcOption = document.getElementById('llcOption');
    const individualOption = document.getElementById('individualOption');
    const innForm = document.getElementById('innNumber');

    function updateInnMaxLength() {
        if (llcOption.checked) {
            innForm.setAttribute('maxlength', '10');
            innForm.setAttribute('minlength', '10');
        } else {
            innForm.setAttribute('maxlength', '12');
            innForm.setAttribute('minlength', '12');
        }
        // Очищаем значение инпута после изменения длины
        innForm.value = '';
    }

    // Обработчики событий для переключения радио кнопок
    llcOption.addEventListener('change', updateInnMaxLength);
    individualOption.addEventListener('change', updateInnMaxLength);

    // Изначально обновляем видимость форм в зависимости от выбранного варианта
        // Проверяем состояние радиокнопок при загрузке страницы
        if (llcOption.checked || individualOption.checked) {
            updateInnMaxLength(); // Изначально обновляем в зависимости от выбора
        } else {
            // Если ни одна не выбрана, установим checked для первой опции
            llcOption.checked = true;
            updateInnMaxLength();
        }
});



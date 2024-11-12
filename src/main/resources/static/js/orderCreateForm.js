// Переменная для проверки валидности номера счета
let isInvoiceValid = false;

// External Invoice Id STRING
let externalInvoiceId = "";
// Сумма Ордера
let price = 0.00;

// Очистка всех полей
function clearAllFields() {

    // Находим форму по ID
    const orderForm = document.getElementById('orderForm');

    // Находим все элементы input внутри этой формы
    const inputs = orderForm.querySelectorAll('input:not([type="radio"])');

    // Перебираем все найденные input и очищаем
    inputs.forEach((input) => {
        input.value = "";
    });

    // Удаляем все выбранные фото
    const imageBlocks = document.querySelectorAll('[id^="imageBlock"]');
    imageBlocks.forEach((block) => {
        block.remove();  // Удаляет элемент из DOM
    });

    // Блокирую поле ввода товара
    let productNameInput = document.getElementById("productName");
    productNameInput.disabled = true;
    productNameInput.value = "";
    // Блокируем ввод количество товара
    productQuantity.disabled = true;
    productQuantity.value = '';

}

// Получаем ФИО Физ лица
function getFullName() {
    let lastNameAgent = document.getElementById("lastNameAgent").value.trim();
    let nameAgent = document.getElementById("nameAgent").value.trim();
    let surNameAgent = document.getElementById("surNameAgent").value.trim();
    let fullNameAgent = `${lastNameAgent} ${nameAgent} ${surNameAgent}`;

    if (lastNameAgent && nameAgent && surNameAgent) {
        return `${lastNameAgent} ${nameAgent} ${surNameAgent}`.trim();
    }
    return null;
}

// Проверка Контрагент физ лицо
function validateAgentByName() {
    let fullNameAgent = getFullName();
    if (fullNameAgent) {
        let params = new URLSearchParams({fullNameAgent: fullNameAgent});

        fetch('/order/validate-agent-by-name?' + params.toString())
            .then(response => {
                    if (!response.ok) {
                        isInvoiceValid = false; // Сбрасываем флаг валидности
                        return response.text().then(text => { throw new Error(text) });
                    }
                    isInvoiceValid = true; // Если инн существует, устанавливаем флаг в true
                    fieldInvoiceExternalId = "";
                    nameAgentErrorForm.textContent = "";  // Убираем сообщение об ошибке если номер и дата валидны
                })
                .catch(error => {
                    isInvoiceValid = false; // Сбрасываем флаг валидности
                    nameAgentErrorForm.textContent = error.message;  // Выводим сообщение об ошибке
            });
    }
}


document.addEventListener("DOMContentLoaded", function() {
  try {
    // Находим все элементы с классом popover-input
    const inputs = document.querySelectorAll('.popover-input');

    // Проходимся по каждому input и инициализируем popover
      inputs.forEach(input => {
        const popover = new bootstrap.Popover(input, {
          trigger: 'manual',
          placement: 'top',
          content: input.getAttribute('data-bs-content')
        });

        // Событие focus для показа popover
        input.addEventListener('focus', () => {
          popover.show();
        });

        // Событие blur для скрытия popover
        input.addEventListener('blur', () => {
          popover.hide();
        });
      });

  } catch (error) {
    console.error("Ошибка инициализации popover:", error);
  }
});

// Проверка ИНН
function validateInn() {

    const input = document.getElementById("innNumber");
    const minLength = input.getAttribute("minlength");
    let innNumber = document.getElementById("innNumber").value;

    //Если символов недостаточно не делать проверку
    if(innNumber.length < minLength) return;

    let invoiceNumber = document.getElementById("invoiceNumber");
    let invoiceDate = document.getElementById("invoiceDate");
    let productName = document.getElementById("productName");
    let productExternalId = document.getElementById("productExternalId");
    let productQuantity = document.getElementById("productQuantity");
    let fieldInvoiceExternalId = document.getElementById("fieldInvoiceExternalId");
    let successOrderMessage = document.getElementById("successOrderCreated");
    let productError = document.getElementById("productError");

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
                productError.textContent = "";
                productQuantityError.textContent = "";

                // Блокирую поле ввода товара
                productName.disabled = true;
                productName.value = "";
                // Блокируем ввод количество товара
                productQuantity.disabled = true;
                productQuantity.value = '';
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
    let innNumber = document.getElementById("innNumber").value;

    if (invoiceNumber.length > 0 && invoiceDate.length > 0) {

        let fullNameAgent = getFullName();
        if (fullNameAgent) {
            innNumber = null;
        } else {
            fullNameAgent = null;
        }

        let invoiceError = document.getElementById("invoiceError");
        let invoiceExternalId = document.getElementById("fieldInvoiceExternalId");
        let productName = document.getElementById("productName");

        let params = new URLSearchParams({ agentInn: innNumber,
                                            agentFullName: fullNameAgent,
                                            invoiceNumber: invoiceNumber,
                                            invoiceDate: invoiceDate });


        let productNameInput = document.getElementById("productName");
        let productQuantity = document.getElementById("productQuantity");

        fetch('/order/validate-invoice?' + params.toString())
            .then(response => {
               return response.text().then(text => {
                   if (!response.ok) {
                      externalInvoiceId = "";
                      // Блокирую поле ввода товара
                      productNameInput.disabled = true;
                      productNameInput.value = "";
                      // Блокируем ввод количество товара
                      productQuantity.disabled = true;
                      productQuantity.value = '';

                      throw new Error(text); // Бросаем ошибку с текстом ответа
                   }
                   productNameInput.disabled = false;
                   if(productName) {
                   console.log("SEND PRODUCT TO CHECK")
                        // Отправлю на проверку товар если есть
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

        console.log("SEARCH PRODUCT START")
        let productNameInput = document.getElementById("productName");
        let productName = document.getElementById("productName").value;
        let suggestions = document.getElementById("productSuggestions");
        let productQuantity = document.getElementById("productQuantity");

        // Блокируем ввод количество товара

        productQuantity.disabled = true;
        productQuantity.value = '';

        let productNameRect = productNameInput.getBoundingClientRect(); // Получаем положение инпута
        let windowHeight = window.innerHeight; // Высота окна браузера
        let spaceBelow = windowHeight - productNameRect.bottom; // Свободное место под инпутом

        if (spaceBelow < 150) { // Если места под инпутом меньше, чем нужно для списка
            suggestions.classList.add('above');
            suggestions.classList.remove('below');
        } else {
            suggestions.classList.add('below');
            suggestions.classList.remove('above');
        }

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
                 productQuantity.disabled = false;
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

 // Маска для телефона, только если элемент существует
 if ($('input[name="phone"]').length) {
     $('input[name="phone"]').inputmask("+7 (999) 999-99-99");
 }
  // Маска для номера карты, только если элемент существует
  if ($('input[name="card"]').length) {
      $('input[name="card"]').inputmask("9999 9999 9999 9999");
  }

 // Находим форму по id
 const form = $('#orderForm');
 const successMessage = $('#successOrderCreated');
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
          return;
          }

          let dataPayment = "Нет данных";
          let typeOrder = "action";
          let paymentOption = 0;
          let paymentOptionExists = document.querySelector('input[name="paymentOption"]');

        if (paymentOptionExists) {
            paymentOption = parseInt(document.querySelector('input[name="paymentOption"]:checked').value, 10);
            typeOrder = "bonus";
                if (phoneOption.checked) {
                    dataPayment = "Оплата по номеру телефона: " + document.getElementById('phoneNumber').value + "<br>в банке " +
                    document.getElementById('bankByPhoneNumber').value + "<br>получатель платежа " +
                    document.getElementById('lastNameByPhoneNumber').value + " " + document.getElementById('nameByPhoneNumber').value;
                }
                if (bankOption.checked) {
                    dataPayment = "Оплата на счёт: " + document.getElementById('accountNumber').value + "<br>в банке " +
                    document.getElementById('bankname').value + "<br>получатель платежа " +
                    document.getElementById('lastNameByAccount').value + " " + document.getElementById('nameByAccount').value;
                }
                if (cardOption.checked) {
                    dataPayment = "Оплата по номеру карты: " + document.getElementById('cardNumber').value + "<br>получатель платежа " +
                    document.getElementById('lastNameByCard').value + " " + document.getElementById('nameByCard').value;
                }
        } else {
            price = 0;
        }
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
             dataPayment: dataPayment,
             type: typeOrder,
             paymentType: {
                id: paymentOption
             }
         };
                 // Скрываем предыдущие сообщения
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

                     // Проверка на наличие details с uri=/403
                     if (responseJson && responseJson.details && responseJson.details.includes("uri=/403")) {
                         window.location.href = "/403"; // Перенаправление на страницу 403
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

// JavaScript для управления отображением форм выбора способа оплаты
document.addEventListener('DOMContentLoaded', function() {

    if(!document.getElementById('phoneOption')) {
        return;
    }
    const phoneOption = document.getElementById('phoneOption');
    const bankOption = document.getElementById('bankOption');
    const cardOption = document.getElementById('cardOption');
    const phoneForm = document.getElementById('phoneForm');
    const bankForm = document.getElementById('bankForm');
    const cardForm = document.getElementById('cardForm');
    const inputsPhone = document.querySelectorAll('.phone-form');
    const inputsBank = document.querySelectorAll('#bankForm .form-control');
    const inputsCard = document.querySelectorAll('#cardForm .form-control');

    function updateFormVisibility() {
        if (phoneOption.checked) {
            // Применяем атрибут required ко всем найденным inputsPhone
            inputsPhone.forEach(input => {
                input.setAttribute('required', '');
            });
            inputsBank.forEach(input => {
                input.removeAttribute('required');
            });
            inputsCard.forEach(input => {
                input.removeAttribute('required');
            });
            phoneForm.style.display = 'block';
            bankForm.style.display = 'none';
            cardForm.style.display = 'none';
        }
        if (cardOption.checked) {
            inputsCard.forEach(input => {
                input.setAttribute('required', '');
            });
           inputsPhone.forEach(input => {
                input.removeAttribute('required');
            });
            inputsBank.forEach(input => {
                input.removeAttribute('required', '');
            });
            bankForm.style.display = 'none';
            phoneForm.style.display = 'none';
            cardForm.style.display = 'block';
        }
        if (bankOption.checked){
            inputsBank.forEach(input => {
                input.setAttribute('required', '');
            });
            inputsPhone.forEach(input => {
                input.removeAttribute('required');
            });
            inputsCard.forEach(input => {
                input.removeAttribute('required');
            });
            bankForm.style.display = 'block';
            phoneForm.style.display = 'none';
            cardForm.style.display = 'none';
        }
    }

    // Обработчики событий для переключения радио кнопок
    phoneOption.addEventListener('change', updateFormVisibility);
    bankOption.addEventListener('change', updateFormVisibility);
    cardOption.addEventListener('change', updateFormVisibility);

    // Изначально обновляем видимость форм в зависимости от выбранного варианта
    updateFormVisibility();
});

// JavaScript для управления отображением форм выбора типа Покупателя
document.addEventListener('DOMContentLoaded', function() {

    const blockByName = document.getElementById('form-by-name');
    const blockByInn = document.getElementById('form-by-inn');

    const llcOption = document.getElementById('llcOption');
    const individualOption = document.getElementById('individualOption');
    const fizOption = document.getElementById('fizOption');

    const innForm = document.getElementById('innNumber');

    // Установка Required для Input
    function setAllInputsToRequired(nameBlock) {

        // Находим все элементы input внутри этого блока
        const inputs = nameBlock.querySelectorAll('input');
        // Перебираем все найденные input и очищаем
        inputs.forEach((input) => {
            input.setAttribute('required', '');
        });
    }
    // Удаление Required для Input
    function removeAllInputsToRequired(nameBlock) {
        // Находим все элементы input внутри этого блока
        const inputs = nameBlock.querySelectorAll('input');
        // Перебираем все найденные input и очищаем
        inputs.forEach((input) => {
            input.removeAttribute('required');
        });
    }


    function updateInnMaxLength() {

        clearAllFields();

        if (llcOption.checked) {

            innForm.setAttribute('maxlength', '10');
            innForm.setAttribute('minlength', '10');
            blockByInn.style.display = 'block';
            blockByName.classList.add('d-none');
            blockByName.classList.remove('d-flex');
            fizOption.checked = false;
            removeAllInputsToRequired(blockByName);
            setAllInputsToRequired(blockByInn);
        } else {

            innForm.setAttribute('maxlength', '12');
            innForm.setAttribute('minlength', '12');
            blockByInn.style.display = 'block';
            blockByName.classList.add('d-none');
            blockByName.classList.remove('d-flex');
            fizOption.checked = false;
            removeAllInputsToRequired(blockByName);
            setAllInputsToRequired(blockByInn);
        }
        // Очищаем значение инпута после изменения длины
        innForm.value = '';
    }

    function visibleBlock() {
        if(fizOption.checked) {

            blockByInn.style.display = 'none';
            blockByName.style.display = 'block';
            llcOption.checked = false;
            individualOption.checked = false;
            setAllInputsToRequired(blockByName);
            removeAllInputsToRequired(blockByInn)
            blockByName.classList.add('d-flex');
            blockByName.classList.remove('d-none');
        }
    }

    // Обработчики событий для переключения радио кнопок
    llcOption.addEventListener('change', updateInnMaxLength);
    individualOption.addEventListener('change', updateInnMaxLength);
    fizOption.addEventListener('change', visibleBlock);

    // Изначально обновляем видимость форм в зависимости от выбранного варианта
    // Проверяем состояние радиокнопок при загрузке страницы
    if (fizOption.checked) {
        removeAllInputsToRequired(blockByInn);
        setAllInputsToRequired(blockByName);
        visibleBlock();
        llcOption.checked = false;
        individualOption.checked = false;
    } else if (llcOption.checked || individualOption.checked) {
        removeAllInputsToRequired(blockByName);
        setAllInputsToRequired(blockByInn);
        updateInnMaxLength(); // Изначально обновляем в зависимости от выбора
        blockByName.style.display = 'none';
    } else {
        // Если ни одна не выбрана, установим checked для первой опции
        llcOption.checked = true;
        updateInnMaxLength();
        blockByName.style.display = 'none';
    }
});

// Функция для получения данных по BIC
async function fetchBICInfo(bic) {
    const url = `https://bik-info.ru/api.html?type=json&bik=${bic}`;
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error('Ошибка сети');
        }
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('Ошибка получения данных:', error);
        return null;
    }
}

// Функция для декодирования HTML-сущностей
function decodeHtmlEntities(text) {
    const tempElement = document.createElement('textarea');
    tempElement.innerHTML = text;
    return tempElement.value;
}

// Функция для обновления полей на странице
function updateFields(data) {
    document.getElementById('bankname').value = decodeHtmlEntities(data.name || 'Нет данных');
    document.getElementById('namemini').textContent = decodeHtmlEntities(data.namemini || 'Нет данных');
    document.getElementById('city').textContent = decodeHtmlEntities(data.city || 'Нет данных');
    document.getElementById('address').textContent = decodeHtmlEntities(data.address || 'Нет данных');
}

// Событие при потере фокуса с поля BIC
// Обработчик события blur
async function handleBlur() {
    const bic = document.getElementById('bic').value;

    // Проверяем, что длина введённого значения соответствует требованиям
    if (bic.length === 9) {
        const data = await fetchBICInfo(bic);
        if (data) {
            updateFields(data);
        }
    } else {
        alert('BIC должен содержать ровно 9 символов!');
    }
}
